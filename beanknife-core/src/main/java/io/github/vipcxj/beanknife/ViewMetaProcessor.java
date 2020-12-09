package io.github.vipcxj.beanknife;

import com.google.auto.service.AutoService;
import io.github.vipcxj.beanknife.annotations.ViewMeta;
import io.github.vipcxj.beanknife.annotations.ViewMetas;
import io.github.vipcxj.beanknife.annotations.ViewOf;
import io.github.vipcxj.beanknife.annotations.ViewOfs;
import io.github.vipcxj.beanknife.models.MetaContext;
import io.github.vipcxj.beanknife.models.Type;
import io.github.vipcxj.beanknife.models.ViewMetaData;
import io.github.vipcxj.beanknife.models.ViewOfData;
import io.github.vipcxj.beanknife.utils.CollectionUtils;
import io.github.vipcxj.beanknife.utils.Self;
import io.github.vipcxj.beanknife.utils.Utils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@SupportedAnnotationTypes({"io.github.vipcxj.beanknife.annotations.ViewMeta", "io.github.vipcxj.beanknife.annotations.ViewMetas"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ViewMetaProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (element.getKind() == ElementKind.CLASS) {
                    List<AnnotationMirror> annotationMirrors = Utils.extractAnnotations(
                            processingEnv,
                            element,
                            "io.github.vipcxj.beanknife.annotations.ViewMeta",
                            "io.github.vipcxj.beanknife.annotations.ViewMetas"
                    );
                    TypeElement configElement = (TypeElement) element;
                    Set<String> targetClassNames = new HashSet<>();
                    for (AnnotationMirror annotationMirror : annotationMirrors) {
                        ViewMetaData viewMeta = ViewMetaData.read(processingEnv, annotationMirror, configElement);
                        TypeElement targetElement = viewMeta.getOf();
                        TypeElement mostImportantViewMetaElement = getMostImportantViewMetaElement(roundEnv, targetElement);
                        if (mostImportantViewMetaElement != null && !Objects.equals(configElement, mostImportantViewMetaElement)) {
                            Type genType = Utils.extractGenType(
                                    Type.extract(targetElement.asType()),
                                    viewMeta.getValue(),
                                    viewMeta.getPackageName(),
                                    "Meta"
                            ).withoutParameters();
                            Utils.logWarn(
                                    processingEnv,
                                    "The meta class \"" +
                                            genType.getQualifiedName() +
                                            "\" which configured on \"" +
                                            configElement.getQualifiedName() +
                                            "\" will not be generated, " +
                                            "because the class \"" +
                                            mostImportantViewMetaElement.getQualifiedName() +
                                            "\" has configured a similar meta class and has a higher priority.");
                            continue;
                        }
                        List<ViewOfData> viewOfDataList = collectViewOfs(roundEnv, targetElement);
                        MetaContext context = new MetaContext(processingEnv, viewMeta, viewOfDataList);
                        String genQualifiedName = context.getGenType().getQualifiedName();
                        if (!targetClassNames.contains(genQualifiedName)) {
                            targetClassNames.add(genQualifiedName);
                            try {
                                writeBuilderFile(context);
                            } catch (IOException e) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                            }
                        } else {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Repeated ViewMeta annotation with class name: " + genQualifiedName + ".");
                        }
                    }
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "");
                }
            }
        }
        return true;
    }

    private void writeBuilderFile(MetaContext context) throws IOException {
        String metaClassName = context.getGenType().getQualifiedName();
        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(metaClassName, context.getViewMeta().getOf());
        try (PrintWriter writer = new PrintWriter(sourceFile.openWriter())) {
            context.collectData();
            context.print(writer);
        }
    }

    private TypeElement getMostImportantViewMetaElement(RoundEnvironment roundEnv, TypeElement targetElement) {
        Set<? extends Element> candidates = roundEnv.getElementsAnnotatedWith(ViewMeta.class);
        List<TypeElement> out = new ArrayList<>();
        for (Element candidate : candidates) {
            if (Utils.shouldIgnoredElement(candidate)) {
                continue;
            }
            List<? extends AnnotationMirror> annotationMirrors = processingEnv.getElementUtils().getAllAnnotationMirrors(candidate);
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                if (((TypeElement) annotationMirror.getAnnotationType().asElement()).getQualifiedName().toString().equals(ViewMeta.class.getCanonicalName())) {
                    if (Utils.isViewMetaTargetTo(processingEnv, annotationMirror, (TypeElement) candidate, targetElement)) {
                        out.add((TypeElement) candidate);
                        break;
                    }
                }
            }
        }
        candidates = roundEnv.getElementsAnnotatedWith(ViewMetas.class);
        for (Element candidate : candidates) {
            if (Utils.shouldIgnoredElement(candidate)) {
                continue;
            }
            List<? extends AnnotationMirror> annotationMirrors = processingEnv.getElementUtils().getAllAnnotationMirrors(candidate);
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                if (((TypeElement) annotationMirror.getAnnotationType().asElement()).getQualifiedName().toString().equals(ViewMetas.class.getCanonicalName())) {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValuesWithDefaults = processingEnv.getElementUtils().getElementValuesWithDefaults(annotationMirror);
                    List<AnnotationMirror> viewMetas = Utils.getAnnotationElement(annotationMirror, elementValuesWithDefaults);
                    for (AnnotationMirror viewMeta : viewMetas) {
                        if (Utils.isViewMetaTargetTo(processingEnv, viewMeta, (TypeElement) candidate, targetElement)) {
                            out.add((TypeElement) candidate);
                            break;
                        }
                    }
                }
            }
        }
        if (!out.isEmpty()) {
            out.sort(Comparator.comparing(e -> e.getQualifiedName().toString()));
            return out.get(0);
        } else {
            return null;
        }
    }

    private void collectViewOfs(List<ViewOfData> results, TypeElement candidate, TypeElement targetElement, AnnotationMirror viewOf) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValuesWithDefaults = processingEnv.getElementUtils().getElementValuesWithDefaults(viewOf);
        Map<String, ? extends AnnotationValue> attributes = CollectionUtils.mapKey(elementValuesWithDefaults, e -> e.getSimpleName().toString());
        TypeElement target = (TypeElement) ((DeclaredType) attributes.get("value").getValue()).asElement();
        if (target.getQualifiedName().toString().equals(Self.class.getCanonicalName())) {
            target = candidate;
        }
        if (!target.equals(targetElement)) {
            return;
        }
        TypeElement config = (TypeElement) ((DeclaredType) attributes.get("config").getValue()).asElement();
        if (config.getQualifiedName().toString().equals(Self.class.getCanonicalName())) {
            config = candidate;
        }
        ViewOfData viewOfData = ViewOfData.read(processingEnv, viewOf, candidate);
        viewOfData.setConfigElement(config);
        viewOfData.setTargetElement(target);
        results.add(viewOfData);
    }

    private List<ViewOfData> collectViewOfs(RoundEnvironment roundEnv, TypeElement targetElement) {
        Set<? extends Element> candidates = roundEnv.getElementsAnnotatedWith(ViewOf.class);
        List<ViewOfData> out = new ArrayList<>();
        for (Element candidate : candidates) {
            if (Utils.shouldIgnoredElement(candidate)) {
                continue;
            }
            List<? extends AnnotationMirror> annotationMirrors = processingEnv.getElementUtils().getAllAnnotationMirrors(candidate);
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                if (((TypeElement) annotationMirror.getAnnotationType().asElement()).getQualifiedName().toString().equals(ViewOf.class.getCanonicalName())) {
                    collectViewOfs(out, (TypeElement) candidate, targetElement, annotationMirror);
                }
            }
        }
        candidates = roundEnv.getElementsAnnotatedWith(ViewOfs.class);
        for (Element candidate : candidates) {
            if (Utils.shouldIgnoredElement(candidate)) {
                continue;
            }
            List<? extends AnnotationMirror> annotationMirrors = processingEnv.getElementUtils().getAllAnnotationMirrors(candidate);
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                if (((TypeElement) annotationMirror.getAnnotationType().asElement()).getQualifiedName().toString().equals(ViewOfs.class.getCanonicalName())) {
                    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValuesWithDefaults = processingEnv.getElementUtils().getElementValuesWithDefaults(annotationMirror);
                    List<AnnotationMirror> viewOfs = Utils.getAnnotationElement(annotationMirror, elementValuesWithDefaults);
                    for (AnnotationMirror viewOf : viewOfs) {
                        collectViewOfs(out, (TypeElement) candidate, targetElement, viewOf);
                    }
                }
            }
        }
        return out;
    }
}
