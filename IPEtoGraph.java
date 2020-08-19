import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class IPEtoGraph {

    static int vertex_count = 0;

    static int Find_closest_index(int [][] vertex_name_cords, int[] vertex)
    {
        double dist = Double.MAX_VALUE;
        int index_to_return = -1;
        for (int i = 0; i < vertex_count; i++)
        {
            if (Math.sqrt(Math.pow((vertex_name_cords[i][0] - vertex[0]), 2) + Math.pow(vertex_name_cords[i][1] - vertex[1], 2)) < dist)
            {
                dist = Math.sqrt(Math.pow((vertex_name_cords[i][0] - vertex[0]), 2) + Math.pow(vertex_name_cords[i][1] - vertex[1], 2));
                index_to_return = i;
            }
        }
        return index_to_return;
    }

    static int which_vertex(int [][] vertex_cord, int x, int y)     //Find vertex number given X and Y coordinates
    {
        for (int i = 0; i < vertex_count; i++) {
            if (vertex_cord[i][0] == x && vertex_cord[i][1] == y)
            {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args)
        throws SAXException, IOException, ParserConfigurationException
    {
        String filename;
        var in = new Scanner(System.in);
        System.out.println("Input file : ");        //Ask user for name of input IPE xml file
        filename = in.nextLine();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc =  builder.parse(filename);
        Element root = doc.getDocumentElement();
        Node page = root.getElementsByTagName("page").item(0);
        NodeList contents = page.getChildNodes();
        int length = contents.getLength();
        for (int i = 0; i < length; i++) {
            if (contents.item(i).getNodeName().equals("text")) {
                vertex_count++;
            }
        }

        int [][] vertex_name_coordinates = new int[vertex_count][2];
        int [][] vertex_coordinates = new int[vertex_count][2];

        for (int i = 0; i < length; i++) {
            if (contents.item(i).getNodeName().equals("text")) {
                String pos = contents.item(i).getAttributes().getNamedItem("pos").toString();
                pos = pos.substring(5, pos.length() - 1);
                vertex_name_coordinates[Integer.parseInt(contents.item(i).getTextContent())][0] = Integer.parseInt(pos.split(" ")[0]);
                vertex_name_coordinates[Integer.parseInt(contents.item(i).getTextContent())][1] = Integer.parseInt(pos.split(" ")[1]);
            }
        }
        for (int i = 0; i < length; i++) {
            if (contents.item(i).getNodeName().equals("use")) { // Assuming use is always a mark
                String pos = contents.item(i).getAttributes().getNamedItem("pos").toString();
                pos = pos.substring(5, pos.length() - 1);
                int []temp = new int[2];
                temp[0] = Integer.parseInt(pos.split(" ")[0]);
                temp[1] = Integer.parseInt(pos.split(" ")[1]);
                int index = Find_closest_index(vertex_name_coordinates, temp);
                vertex_coordinates[index][0] = temp[0];
                vertex_coordinates[index][1] = temp[1];
            }
        }

        int edge_counter = 0;
        for (int i = 0; i < length; i++)
            if (contents.item(i).getNodeName().equals("path")) {
                String[] check_loop = contents.item(i).getTextContent().split(System.getProperty("line.separator"));
                if (!check_loop[1].substring(0, check_loop[1].length() - 2).equals(check_loop[check_loop.length - 1].substring(0, check_loop[check_loop.length - 1].length() - 2)))
                    edge_counter++;
            }
        FileWriter fileWriter = new FileWriter(filename.substring(0, filename.length() - 3) + "txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.printf("%d %d\n", vertex_count, edge_counter * 2);


        for (int i = 0; i < length; i++) {
            if (contents.item(i).getNodeName().equals("path"))
            {
                String[] edge_set = contents.item(i).getTextContent().split(System.getProperty("line.separator")); // First thing in the array is always a newline
                int source, destination;
                source = which_vertex(vertex_coordinates, Integer.parseInt(edge_set[1].split(" ")[0]), Integer.parseInt(edge_set[1].split(" ")[1]));
                destination = which_vertex(vertex_coordinates, Integer.parseInt(edge_set[edge_set.length - 1].split(" ")[0]), Integer.parseInt(edge_set[edge_set.length - 1].split(" ")[1]));
                if (source == destination || source == -1 || destination == -1)
                    continue;
                printWriter.printf("%d %d\n%d %d\n", source, destination, destination, source);
            }
        }
        printWriter.close();
    }
}