package reflection.util;

@MyAnnotation
public class Foo {
    private String fooName = "foo";
    public String fooPublicName = "fooPublicName";

    @MyAnnotation
    private String say() {
        return this.fooName + " say hello";
    }
    private String say(String name) {
        return this.fooName + " say hello to " + name;
    }
    public String shout() {
        return this.fooName + " shout";
    }

    @MyAnnotation
    public Foo(String name) {
        this.fooName = name;
    }

    @MyAnnotation
    public Foo() {
    }

    public String getFooName() {
        return fooName;
    }

    public void setFooName(String fooName) {
        this.fooName = fooName;
    }

    public String getFooPublicName() {
        return fooPublicName;
    }

    public void setFooPublicName(String fooPublicName) {
        this.fooPublicName = fooPublicName;
    }

    @Override
    public String toString() {
        return "Foo{" +
                "fooName='" + fooName + '\'' +
                ", fooPublicName='" + fooPublicName + '\'' +
                '}';
    }
}
