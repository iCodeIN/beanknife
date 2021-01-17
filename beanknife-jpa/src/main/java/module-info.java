import io.github.vipcxj.beanknife.core.spi.ViewCodeGenerator;
import io.github.vipcxj.beanknife.jpa.JpaViewCodeGenerator;

module beanknife.jpa {
    requires beanknife.core;
    requires beanknife.jpa.runtime;
    requires java.compiler;
    provides ViewCodeGenerator with JpaViewCodeGenerator;
}