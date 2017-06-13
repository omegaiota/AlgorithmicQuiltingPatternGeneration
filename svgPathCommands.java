package jackiesvgprocessor;

/**
 * Created by JacquelineLi on 6/13/17.
 */
public class svgPathCommands {
    private String command;
    private double x0,y0,x1,y1,x2,y2;

    public static void parseString(String commandString) {
        String[] arguments = commandString.split(" ");
        for (String arg : arguments) {
            System.out.println(arg);
        }
        if (arguments.equals("m")) {
            System.out.println("is moveTo");
        }
    }
}
