package io.github.vipcxj.beanknife.runtime.annotations;

import io.github.vipcxj.beanknife.runtime.utils.Self;

import java.lang.annotation.*;

/**
 * Used to generate the DTO type.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(ViewOfs.class)
public @interface ViewOf {
    /**
     * The targetType
     * @return the target type. By default the annotated class is used.
     */
    Class<?> value() default Self.class;

    /**
     * The configType
     * @return the config type. By default the annotated class is used.
     */
    Class<?> config() default Self.class;

    /**
     * The package name of the generated class. By default the package name of the target class is used.
     * @return the package name of the generated class
     */
    String genPackage() default "";
    /**
     * The simple name of the generated class. By default the simple name of the target class + View is used.
     * @return the simple name of the generated class
     */
    String genName() default "";

    /**
     * The access type of the generated class.
     * @return the access type of the generated class.
     */
    Access access() default Access.PUBLIC;

    /**
     * The included properties. By default, nothing is included.
     * @return the included properties
     */
    String[] includes() default {};
    /**
     * The excluded properties. By default, nothing is excluded.
     * @return the excluded properties
     */
    String[] excludes() default {};
    /**
     * The regex pattern of the included properties. By default, nothing is included.
     * @return the regex pattern of the included properties
     */
    String includePattern() default "";
    /**
     * The regex pattern of the excluded properties. By default, nothing is excluded.
     * @return the regex pattern of the excluded properties
     */
    String excludePattern() default "";

    /**
     * The access type of the empty constructor. By default, public is used.
     * @return the access type of the empty constructor
     */
    Access emptyConstructor() default Access.PUBLIC;
    /**
     * The access type of the field constructor. By default, public is used.
     * @return the access type of the field constructor
     */
    Access fieldsConstructor() default Access.PUBLIC;
    /**
     * The access type of the copy constructor. By default, public is used.
     * @return the access type of the copy constructor
     */
    Access copyConstructor() default Access.PUBLIC;
    /**
     * The access type of the getter methods. By default, public is used.
     * It can be override by the {@link ViewProperty}, {@link OverrideViewProperty} and {@link NewViewProperty}.
     * @return the access type of the getter methods
     */
    Access getters() default Access.PUBLIC;
    /**
     * The access type of the setter methods. By default, none is used, which means there are no setter method.
     * It can be override by the {@link ViewProperty}, {@link OverrideViewProperty} and {@link NewViewProperty}.
     * @return the access type of the setter methods
     */
    Access setters() default Access.NONE;

    /**
     * Used to control whether to add the error methods. Default is true.
     * @return whether to add the error methods
     */
    boolean errorMethods() default true;
}
