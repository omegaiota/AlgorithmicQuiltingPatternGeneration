package jackiesvgprocessor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by JacquelineLi on 6/12/17.
 */
public class fileProcessor {
    private  Path fFilePath;
    private  String fFileName;
    private  File fSvgFile;
    private  NodeList pathNodeList;

    public fileProcessor(File importFile) {
        this.fFilePath = Paths.get(importFile.getPath());
        this.fFileName = importFile.getName();
        this.fSvgFile = importFile;
    }

    public void processSvg() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(fSvgFile);

        String xpathExpression = "//path/@d";
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile(xpathExpression);
        pathNodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

        for (int i = 0; i < pathNodeList.getLength(); i++) {
            processPath(pathNodeList.item(i));
        }


    }

    private void processPath(Node pathNode) {
        String pathStr = pathNode.getNodeValue();
        System.out.println(pathStr);

    }
}
