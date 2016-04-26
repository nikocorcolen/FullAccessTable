/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fulltablescan;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 *
 * @author CDIAZ
 */
public  class GenerateReport {

    /**
     * 
     * @param dir Directorio donde se guardará el archivo generado
     * @param querys Tiene todas las consultas evaluadas
     */
    public static void GenerateReport(String dir, ArrayList<Report> querys) {
        try {
            // TODO code application logic here
            // Se crea el documento
            Document documento = new Document(PageSize.A4.rotate());
            FileOutputStream ficheroPdf = new FileOutputStream(dir + "\\Reporte.pdf");
            // Se asocia el documento al OutputStream y se indica que el espaciado entre
            // lineas sera de 20. Esta llamada debe hacerse antes de abrir el documento
            PdfWriter.getInstance(documento,ficheroPdf).setInitialLeading(20);
            // Se abre el documento.
            documento.open();
            
            //Numero de columnas
            float[] columnWidths = {1, 10};
            //Se crea la tabla
            PdfPTable table = new PdfPTable(columnWidths);
            table.setWidthPercentage(100);
            table.getDefaultCell().setUseAscender(true);
            table.getDefaultCell().setUseDescender(true);
            
            //Se le agrega un header en blanco
            Font f = new Font(FontFamily.HELVETICA, 13, Font.NORMAL, GrayColor.GRAYWHITE);
            PdfPCell cell = new PdfPCell(new Phrase("", f));
            cell.setBackgroundColor(GrayColor.GRAYBLACK);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setColspan(3);
            table.addCell(cell);
            
            for (int counter = 0; counter < querys.size(); counter++) {
                
                table = new PdfPTable(columnWidths);//Se crea una nueva tabla para la sigueinte consulta
                table.setWidthPercentage(100);
                table.getDefaultCell().setUseAscender(true);
                table.getDefaultCell().setUseDescender(true);
                
                //Titulo de la tabla
                table.getDefaultCell().setBackgroundColor(new GrayColor(0.75f));
                table.addCell("-");
                table.addCell("Valor");
                
                //Config para el cuerpo de la tabla
                table.setHeaderRows(3);
                table.setFooterRows(1);
                table.getDefaultCell().setBackgroundColor(GrayColor.GRAYWHITE);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                
                
                table.addCell("Consulta ");
                table.addCell(querys.get(counter).getQuery());
                table.addCell("Ejecución ");
                table.addCell(querys.get(counter).getExecTime());
                table.addCell("Full Access ");
                table.addCell(querys.get(counter).isFullAccess() + "");
                
                documento.add(table);
                
                documento.add(new Paragraph("\n"));//Espacio en blanco
            }
            
            documento.close();
            
        } catch (DocumentException | FileNotFoundException ex) {
            System.out.println(ex.getMessage() + "\n" +  ex);
            System.out.println("Error al generar el reporte");
            System.exit(-1);
        }
    }
}
