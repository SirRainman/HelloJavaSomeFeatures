package createPattern.SimpleFactory;

/**
 * @program: DesignPattern
 * @description:
 * @author: Rain
 * @create: 2021-05-19 12:38
 **/
public class Rectangle implements Shape {

    @Override
    public void draw() {
        System.out.println("Inside Rectangle::draw() method.");
    }
}