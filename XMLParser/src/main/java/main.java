import java.util.Scanner;
public class main {
    public static void main(String[] args) throws ClassNotFoundException {
        Scanner myObj = new Scanner(System.in);
        System.out.println("Enter the directory to mains243.xml:");
        String mainXml = myObj.nextLine();
        System.out.println("Enter the directory to actors63.xml:");
        String actorsXml = myObj.nextLine();
        System.out.println("Enter the directory to casts124.xml:");
        String castsXml = myObj.nextLine();

        System.out.println("Start to parse mains243.xml...");
        XMLParser xp = new XMLParser(mainXml);
        xp.startParsing();
        System.out.println("Done!");

        System.out.println("Start to parse actors63.xml...");
        XMLParser_stars xps = new XMLParser_stars(actorsXml);
        xps.startParsing();
        System.out.println("Done!");

        System.out.println("Start to parse casts124.xml...");
        XMLParser_cast xpc = new XMLParser_cast(castsXml);
        xpc.startParsing();
        System.out.println("Done!");
    }
}
