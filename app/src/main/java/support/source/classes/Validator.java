package support.source.classes;

/**
 * Created by Pankaj Nimgade on 13-06-2016.
 */
public class Validator {

    public static int getNumber(String number) {
        if (number != null) {
            if (!number.equalsIgnoreCase("")) {
                if (number.matches("\\d+")) {
                    printText("its a number");
                    return Integer.parseInt(number);
                }
            }
        }
        return 0;
    }

    public static void printText(String text) {
        System.out.println("" + text);
    }

}
