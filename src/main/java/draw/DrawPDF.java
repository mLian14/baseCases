package draw;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import shapes.*;

import javax.print.Doc;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

import static com.itextpdf.kernel.colors.Color.convertRgbToCmyk;

public class DrawPDF {
    private parser.Document outputDoc;
    private final String path;

    public DrawPDF(parser.Document outputDoc, String path) {
        this.outputDoc = outputDoc;
        this.path = path;
    }

    public void outputToPdf() throws FileNotFoundException {



        ArrayList<Slave> slaves = outputDoc.getSlaves();
        ArrayList<Keepout> uni_keepouts = outputDoc.getKeepouts();
        Master master = outputDoc.getMaster();
        ArrayList<Path> paths = outputDoc.getPaths();
        ArrayList<Node> steinerPoints = outputDoc.getSteinerPoints();
        /*
        Process the PDFsize
         */
        ArrayList<Integer> xs = new ArrayList<>();
        ArrayList<Integer> ys = new ArrayList<>();
        xs.add(master.getX_ct());
        ys.add(master.getY_ct());

        for (Slave sv : slaves){
            xs.add(sv.getX_ct());
            ys.add(sv.getY_ct());
        }
        for (Keepout o : uni_keepouts){
            xs.add(o.getMinX());
            xs.add(o.getMaxX());
            ys.add(o.getMinY());
            ys.add(o.getMaxY());
        }
        int lb_x = Collections.min(xs);
        int ub_x = Collections.max(xs);
        int lb_y = Collections.min(ys);
        int ub_y = Collections.max(ys);
        lb_x -= 200;
        ub_x += 200;
        lb_y -= 200;
        ub_y += 200;
        int width = ub_x - lb_x;
        int height = ub_y - lb_y;
        int sum_wh = width + height;

        double master_r = sum_wh/200;
        double sl_r = sum_wh/350;
        float lineWidth = sum_wh/1000;



        //String pdfPath = this.path + "_" + outputDoc.getI2Cname() + ".pdf";
        String pdfPath = this.path + ".pdf";
        Rectangle rectangle = new Rectangle(lb_x, lb_y, width, height);
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfPath));
        Document document = new Document(pdfDoc, new PageSize(rectangle));


        PdfCanvas canvas = new PdfCanvas(pdfDoc.addNewPage());
        canvas.setLineWidth(lineWidth);

        /*
        draw Keepouts
         */
        float p_bias = 2;
        for (Keepout o : uni_keepouts){
            Rectangle rect = new Rectangle(o.getMinX(), o.getMinY(), o.getMaxX() - o.getMinX(), o.getMaxY() - o.getMinY());
            Color LightBlue = convertRgbToCmyk(new DeviceRgb(173,216,230));
            canvas.setColor(LightBlue, true)
                    .rectangle(rect)
                    .fill()
                    .stroke();
            Color BLACK = convertRgbToCmyk(new DeviceRgb(0,0,0));
            canvas.setColor(BLACK, false)
                    .rectangle(rect)
                    .stroke();
            Paragraph pO = new Paragraph("[" + o.getMinX() + ", " + o.getMaxX() + "]*[" + o.getMinY() + ", " + o.getMaxY() + "]").setFontSize(20).setFontColor(convertRgbToCmyk(new DeviceRgb(0,0,0)));
            pO.setFixedPosition(o.getMinX() + p_bias, (float) ((o.getMinY() + o.getMaxY())*0.5), 2000);
            document.add(pO);
        }

        ArrayList<Point> points = new ArrayList<>();
        points.add(new Point(master.getX_ct(), master.getY_ct()));
        /*
        draw Master
         */
        Color PINK = convertRgbToCmyk(new DeviceRgb(255,182,193));
        canvas.setColor(PINK, true)
                .circle(master.getX_ct(), master.getY_ct(), master_r)
                .fill()
                .stroke();
        Paragraph pMaster = new Paragraph(master.getName() + " (" + master.getX_ct() + ", " + master.getY_ct() + ")").setFontSize(20).setFontColor(convertRgbToCmyk(new DeviceRgb(0,0,0)));
        pMaster.setFixedPosition(master.getX_ct() , master.getY_ct() + p_bias, 200);
        document.add(pMaster);



        /*
        draw Slaves
         */

        for (Slave sv : slaves){
            points.add(new Point(sv.getX_ct(), sv.getY_ct()));
            Color RED = convertRgbToCmyk(new DeviceRgb(255,0,0));
            canvas.setColor(RED, true)
                    .circle(sv.getX_ct(), sv.getY_ct(), sl_r)
                    .fill()
                    .stroke();

            //Paragraph pSV = new Paragraph(sv.getName() +" (" + sv.getX_ct() + ", " + sv.getY_ct() + ")").setFontSize(20).setFontColor(convertRgbToCmyk(new DeviceRgb(0,0,0)));
            Paragraph pSV = new Paragraph(sv.getName() +" (" + sv.getX_ct() + ", " + sv.getY_ct() + ")").setFontSize(20).setFontColor(convertRgbToCmyk(new DeviceRgb(0,0,0)));
            pSV.setFixedPosition(sv.getX_ct() , sv.getY_ct() - p_bias, 2000);
            document.add(pSV);
        }



        /*
        draw paths
         */
        for (Path path : paths){
            Color BLACK = convertRgbToCmyk(new DeviceRgb(0,0,0));
            canvas.setColor(BLACK, false)
                    .moveTo(path.startPoint.getX_exact(), path.startPoint.getY_exact())
                    .lineTo(path.endPoint.getX_exact(), path.endPoint.getY_exact())
                    .closePathStroke();
            if (!points.contains(path.startPoint)){
                canvas.setColor(BLACK, true)
                        .circle(path.startPoint.getX_exact(), path.startPoint.getY_exact(), sl_r)
                        .fill()
                        .stroke();

                //Paragraph pSV = new Paragraph(sv.getName() +" (" + sv.getX_ct() + ", " + sv.getY_ct() + ")").setFontSize(20).setFontColor(convertRgbToCmyk(new DeviceRgb(0,0,0)));
                Paragraph pSV = new Paragraph(" (" + path.startPoint.getX_exact() + ", " + path.startPoint.getY_exact() + ")").setFontSize(20).setFontColor(convertRgbToCmyk(new DeviceRgb(0,0,0)));
                pSV.setFixedPosition((float)path.startPoint.getX_exact() , (float)(path.startPoint.getY_exact() - 20 * p_bias), 2000);
                document.add(pSV);
            }
            if (!points.contains(path.endPoint)){
                canvas.setColor(BLACK, true)
                        .circle(path.endPoint.getX_exact(), path.endPoint.getY_exact(), sl_r/2)
                        .fill()
                        .stroke();

                //Paragraph pSV = new Paragraph(sv.getName() +" (" + sv.getX_ct() + ", " + sv.getY_ct() + ")").setFontSize(20).setFontColor(convertRgbToCmyk(new DeviceRgb(0,0,0)));
                Paragraph pSV = new Paragraph(" (" + path.endPoint.getX_exact() + ", " + path.endPoint.getY_exact() + ")").setFontSize(20).setFontColor(convertRgbToCmyk(new DeviceRgb(0,0,0)));
                pSV.setFixedPosition((float)path.endPoint.getX_exact() , (float)(path.endPoint.getY_exact() - 20 * p_bias), 2000);
                document.add(pSV);

            }
        }

        /*
        draw steiner points
         */
        for (Node steiner : steinerPoints){
            Color RED = convertRgbToCmyk(new DeviceRgb(255,0,0));
            canvas.setColor(RED, true)
                    .circle(steiner.getX(), steiner.getY(), 0.5 * sl_r)
                    .fill()
                    .stroke();

        }




        pdfDoc.close();




    }






}
