package io.github.vipcxj.beanknife.core.models;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.io.PrintWriter;
import java.util.List;

public class StaticMethodExtractor implements Extractor {

    private final Type container;
    private final ExecutableElement executableElement;
    private final Type returnType;

    public StaticMethodExtractor(Context context, Type container, ExecutableElement executableElement) {
        this.container = container;
        this.executableElement = executableElement;
        this.returnType = Type.extract(context, executableElement);
    }

    @Override
    public boolean check(@NonNull ViewContext context, @CheckForNull Property property) {
        Name name = executableElement.getSimpleName();
        if (!executableElement.getModifiers().contains(Modifier.STATIC)) {
            context.error("The static property method \"" + name + "\" should be static.");
            return false;
        }
        if (returnType == null) {
            context.error("The static property method \"" + name + "\" should return a valid type.");
            return false;
        }
        if (property != null && !returnType.equals(property.getType())) {
            context.error("The static property method \"" + name + "\" should return a \"" + returnType + "\" type.");
            return false;
        }
        List<? extends VariableElement> parameters = executableElement.getParameters();
        String sign = "\"" + returnType + " " + name + "()\" or \"" + returnType + " " + name + "(" + context.getTargetType() + " source)\"";
        if (parameters.size() > 1) {
            context.error("The static property method \"" +
                    name +
                    "\" has to many parameters. " +
                    "It should look like " +
                    sign + "."
            );
            return false;
        }
        if (parameters.size() == 1) {
            Type paramType = Type.extract(context, parameters.get(0));
            if (!context.getTargetType().equals(paramType)) {
                context.error("The static property method \"" +
                        name +
                        "\" has wrong parameter. " +
                        "It should look like " +
                        sign + "."
                );
                return false;
            }
        }
        return true;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public void print(PrintWriter writer, Context context) {
        container.printType(writer, context, false, false);
        writer.print(".");
        writer.print(executableElement.getSimpleName());
        writer.print("(source)");
    }
}
