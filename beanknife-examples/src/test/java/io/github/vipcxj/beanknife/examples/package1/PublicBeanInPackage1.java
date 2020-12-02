package io.github.vipcxj.beanknife.examples.package1;

import io.github.vipcxj.beanknife.annotations.ViewMeta;

@ViewMeta
public class PublicBeanInPackage1 {

    public int publicIntField;
    protected int protectedIntField;
    int defaultIntField;
    private int privateIntField;

    public int getPrivateIntField() {
        return privateIntField;
    }
}
