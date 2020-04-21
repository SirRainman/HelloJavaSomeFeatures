package reflection.util;

public class Foo {
    private String fooName = "foo";
    public String fooPublicName;

    private void say() {
        System.out.println("hello");
    }
    private void say(String s) {
        System.out.println("hello" + s);
    }
    public void shout() {
        System.out.println("public: shout");
    }

    public Foo(String name) {
        this.fooName = name;
    }

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
                '}';
    }
}
