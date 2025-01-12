package me.xpyex.plugin.parrot.mirai.module.skripthub;

import cn.hutool.json.JSONObject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
//import org.xhtmlrenderer.swing.Java2DRenderer;  TODO
import org.xml.sax.SAXException;

public class SkriptDocBuilder {

    private final StringJoiner stringJoiner;
    private final List<JSONObject> syntaxList = new ArrayList<>();
    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private SkriptDocBuilder() {
        stringJoiner = new StringJoiner("", "<!DOCTYPE html><html lang=\"zh\"><head><title>Skript Hub - Documentation</title><style>*{margin:0;font-family:\"Lucida Console\",monospace}body{background-color:#e3e3e3}h3{display:inline-block;font-size:32px;padding:7px;font-family:Arial,Helvetica,sans-serif;font-weight:400}.card{margin:20px;border-left:16px solid;background-color:#f9f9f9;box-shadow:rgba(0,0,0,.16) 10px 10px 10px 0}.code-body{padding:7px;background-color:#1e1e1e;color:#dcdcdc;font-size:14px}.card-footer{padding:20px 7px;color:#707070}.card-example{background-color:#fff}.title{color:#000}.addon{float:right;color:#707070}.event{border-color:#a763ff}.condition{border-color:#ff3d3d}.effect{border-color:#0178ff}.expression{border-color:#0de505}.type{border-color:#f39c12}.function{border-color:#b4b4b4}.section{border-color:#1abc9c}.structure{border-color:#e056fd}</style></head>", "</html>");
    }

    public static SkriptDocBuilder builder() {
        return new SkriptDocBuilder();
    }

    public SkriptDocBuilder syntax(JSONObject jsonObject) {
        syntaxList.add(jsonObject);
        return this;
    }

    public BufferedImage build(int width) {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document dom = documentBuilder.newDocument();
            Element body = dom.createElement("body");
            for (JSONObject jb : syntaxList) {
                Element cardDiv = dom.createElement("div");
                cardDiv.setAttribute("class", "card " + jb.getStr("syntax_type"));

                Element cardHeaderDiv = dom.createElement("div");
                cardHeaderDiv.setAttribute("class", "card-header");
                Element title = dom.createElement("h3");
                title.setAttribute("class", "title");
                title.setTextContent(jb.getStr("title"));
                cardHeaderDiv.appendChild(title);
                Element addon = dom.createElement("h3");
                addon.setAttribute("class", "addon");
                addon.setTextContent(jb.getJSONObject("addon").getStr("name") + "-v" + jb.getStr("compatible_addon_version"));
                cardHeaderDiv.appendChild(addon);
                cardDiv.appendChild(cardHeaderDiv);

                Element cardBodyDiv = dom.createElement("div");
                cardBodyDiv.setAttribute("class", "card-body");
                Element syntaxPatternDiv = dom.createElement("div");
                syntaxPatternDiv.setAttribute("class", "code-body");
                Element syntaxSpan = dom.createElement("span");
                syntaxSpan.setTextContent(jb.getStr("syntax_pattern"));
                syntaxPatternDiv.appendChild(syntaxSpan);
                cardBodyDiv.appendChild(syntaxPatternDiv);
                cardDiv.appendChild(cardBodyDiv);

                Element cardFooterDiv = dom.createElement("div");
                cardFooterDiv.setAttribute("class", "card-footer");
                Element descriptionP = dom.createElement("p");
                descriptionP.setTextContent(jb.getStr("description"));
                cardFooterDiv.appendChild(descriptionP);
                cardDiv.appendChild(cardFooterDiv);

                // 好像做不了例子
                // Element cardExampleDiv = dom.createElement("div");
                // cardExampleDiv.setAttribute("class", "card-example");
                // Element exampleDiv = dom.createElement("div");
                // syntaxPatternDiv.setAttribute("class", "code-body");
                // Element exampleSpan = dom.createElement("span");
                // syntaxSpan.setTextContent(jb.getStr("example"));
                // exampleDiv.appendChild(exampleSpan);
                // cardBodyDiv.appendChild(exampleDiv);
                // cardDiv.appendChild(cardExampleDiv);

                body.appendChild(cardDiv);
            }
            dom.appendChild(body);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(dom), result);

            String html = stringJoiner.add(writer.toString()).toString();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
            Document htmlDom = documentBuilder.parse(inputStream);
//            Java2DRenderer java2DRenderer = new Java2DRenderer(htmlDom, width);  TODO
//            return java2DRenderer.getImage();  TODO
            return null;
        } catch (IOException | SAXException | TransformerException | ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

}
