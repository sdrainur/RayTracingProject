import javax.swing.*;

class MyFrame extends JFrame {

    public MyFrame() {
        setSize(Main.C_W, Main.C_H);
        setTitle("RayTracing");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        Print print = new Print();
        add(print);
    }
}
