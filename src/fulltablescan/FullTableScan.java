/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fulltablescan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Nicolas Maturana Barrios
 * @version 1.0
 */
public class FullTableScan {

    //Variable que maneja la conexion a la base de datos
    private static ConnectionDB con;
    //Contiene las consultas ya modificadas con sus variables para ser ejecutadas
    private static ArrayList<Report> reports;
    //Para probar la expresion regular vistar http://regexr.com/
    private static Pattern pattern = Pattern.compile("[\\w]*[.]*[\\w]+[\\s]*[=]*[\\s]*[\\w]*[(]*[#][{][\\w]+[:]*[\\w]*[}]");
    //Consulta que evalua la query para saber si tiene "full access table"
    //se le debe concatenar la consulta
    private static String ex_plan = "EXPLAIN PLAN FOR ";
    //Consulta que muestra si la consulta ejecutada tiene "full access table"
    private static String show_plan = "SELECT PLAN_TABLE_OUTPUT FROM TABLE(DBMS_XPLAN.DISPLAY)";
    
    /**
     * @param args no args!
     */
    public static void main(String[] args) {
        
        System.out.print("Ruta de la carpeta con los archivos: ");
        Scanner entradaEscaner = new Scanner (System.in); //Creación de un objeto Scanner
        String dir = entradaEscaner.nextLine(); //Invocamos un método sobre un objeto Scanner
        
        con = new ConnectionDB(dir);
        LoadQuery q = new LoadQuery(dir);
        ArrayList<String> querys = q.getQuerys();
        System.out.println("Cantidad de querys a evaluar: " + querys.size());
        System.out.println("---------------------------------------------------------------------------------");
        reports = new ArrayList<>();
        
        Report reportVariable;
        
        for (int i = 0; i < querys.size(); i++) {
            reportVariable = new Report();
//            System.out.println("---------------------------------------------------------------------------------");
//            System.out.println(querys.get(i));
            String tempQuery = querys.get(i);
            
            //Busca todas las coincidencias que tenga la consulta con la expresion regular
            Matcher matcher = pattern.matcher(tempQuery);
            
//            System.out.println("Campos requeridos: \n");
            //Reemplaza las coincidencias encontradas con valores obtenidos de la base de datsos
            while (matcher.find()) {
                String match = matcher.group();
//                System.out.println(match);
                String tablaRegistro = match.split("\\=")[0];
                
                //Busca los datos faltantes generando una consulta SQL
                if (tablaRegistro.contains(".")) { 
                        //La tabla esta separada del registro por un punto
//                    System.out.println("SELECT " + tablaRegistro.split("\\.")[1] + " FROM " + tablaRegistro.split("\\.")[0] + " WHERE ROWNUM = 1");
                    //Hacer la consulta a la bd y traer al parametro que me falta y reemplazarlo donde correponda
                    try {
                        String columna = tablaRegistro.split("\\.")[1];
                        String tabla = tablaRegistro.split("\\.")[0];
                        ResultSet r = con.stmt.executeQuery("SELECT " + columna + " FROM " + tabla + " WHERE ROWNUM = 1");
                        r.first();
                        String dbValue = r.getString(1);
                        String matchFixed = match.replaceFirst("[#][{][\\w]+[:]*[\\w]*[}]", "'" + dbValue + "'");
                        tempQuery = tempQuery.replace(match, matchFixed);
                    } catch (SQLException ex) {
                        System.out.println("Error obteniendo valores desde la base de datos");
                        Logger.getLogger(FullTableScan.class.getName()).log(Level.SEVERE, null, ex);
                        System.exit(-1);
                    }
                }
                else{
                    if(match.toUpperCase().contains("TO_DATE")){
//                        System.out.println("Select SYSDATE From Dual");
                        //Hacer la consulta a la bd y traer al parametro que me falta y reemplazarlo donde correponda
                        DateFormat dateFormat = new SimpleDateFormat("dd-MM-YYY");
                        Date date = new Date();
                        String matchFixed = match.replaceFirst("[#][{][\\w]+[:]*[\\w]*[}]", dateFormat.format(date));
                        tempQuery = tempQuery.replace(match, matchFixed);
                    }
                    else{
                        Pattern newPattern = Pattern.compile("\\b[\\w]+(?!\\s)[.]+" + match.split("\\=")[0].replace(" ", "") + "\\b");
                        Matcher newMatcher = newPattern.matcher(tempQuery);
                        if (newMatcher.find()) {
                            String aux = newMatcher.group();
                            String[] result = aux.split("\\.");
                            if (result.length > 1) {
                                try {
    //                                System.out.println("Select " + result[1] + " From " + result[0] + " Limit 1");
                                    String columna = result[1];
                                    String tabla = result[0];
                                    ResultSet r = con.stmt.executeQuery("SELECT " + columna + " FROM " + tabla + " WHERE ROWNUM = 1");
                                    r.first();
                                    String dbValue = r.getString(columna);
                                    //Hacer la consulta a la bd y traer al parametro que me falta y reemplazarlo donde correponda
                                    String matchFixed = aux.replaceFirst("[#][{][\\w]+[:]*[\\w]*[}]", "'" + dbValue + "'");
                                    tempQuery = tempQuery.replace(aux, matchFixed);
                                } catch (SQLException ex) {
                                    Logger.getLogger(FullTableScan.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            else{
//                                System.out.println("No se encontro la tabla perteneciente al registro para generar la consulta");
                            }
                        }
                        else{//No se puede encontrar la tabla a la que pertenece el registro
//                            System.out.println("No se encontro la tabla perteneciente al registro para generar la consulta");
                        }
                    }
                }
            }
            //Se guarda la nueva consulta con sus variables seteadas
            reportVariable.setQuery(tempQuery);
            /*
             * Se valida que la query tenga todos sus parametros para ser ejecutada
             * si no tiene todas sus variables seteadas no se ejecutara, ya que 
             * se producira un error
             */
            if (validateQuery(tempQuery)) {
                try {
//                    String query_full = "SELECT MAX(BLKEVN.BLKEVN_CD) FROM BLKEVN WHERE BLKEVN.BLKEVN_STS IN(1,2,3,5,6,7,11) GROUP BY BLKEVN.BLKSPC_CD";
//                    String query_no_full = "SELECT MAX(BLKEVN.BLKEVN_CD) FROM BLKEVN WHERE BLKEVN.BLKEVN_CD>=0 AND BLKEVN.BLKEVN_STS IN(1,2,3,5,6,7,11) GROUP BY BLKEVN.BLKSPC_CD";
//                    String ex_plan = "EXPLAIN PLAN SET statement_id = 'ex_plan1' FOR ";
//                    String show_plan = "SELECT PLAN_TABLE_OUTPUT FROM TABLE(DBMS_XPLAN.DISPLAY(NULL, 'ex_plan1', 'BASIC'))";        

                    //Ejecuta el Batch para evaluar la consulta
                    con.stmt.addBatch(ex_plan + tempQuery);
                    con.stmt.executeBatch();
                    con.stmt.clearBatch();

                    //Se hace la llamanda para obtener los datos del plan de ejecucion
                    ResultSet rs = con.stmt.executeQuery(show_plan);
                    Boolean fullAccess = false;
                    while (rs.next()) {
                        String line = rs.getString("PLAN_TABLE_OUTPUT");
                        //Para ver los datos entregados por el plan de ejecucion 
                        //descomentar la linea de abajo usar System.out.println(line + "\n");
                        if (line.contains("TABLE ACCESS FULL")) {
                            fullAccess = true;
                        }
                    }
                    reportVariable.setFullAccess(fullAccess);
                    //Se ejecuta la consulta para medir el tiempo de ejecucion
                    reportVariable.setExecTime(execTime(tempQuery));

                } catch (SQLException ex) {
                    System.out.println("Error de conexión a la base de datos");
                    Logger.getLogger(FullTableScan.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            reports.add(reportVariable);
//            System.out.println("Nueva query: " + tempQuery);
        }
        
//        for (int i = 0; i < reports.size(); i++) {
//            System.out.println("---------------------------------------------------------------------------------");
//            System.out.println(reports.get(i).getQuery());
//            System.out.println(reports.get(i).getExecTime());
//            System.out.println("Full access table: " + reports.get(i).isFullAccess());
//        }
        con.close();
        
        GenerateReport.GenerateReport(dir, reports);
    }
    
    /**
     * Mide el tiempo de ejecucion de una consulta SQL
     * En caso de error muestra la consulta que tiene problemas
     * @param query Query a ejecutar
     * @return Tiempo de ejecucion en milisegundos
     */
    public static String execTime(String query){
        long time_start, time_end;
        time_start = System.currentTimeMillis();
        try {
            con.stmt.executeQuery(query);
        } catch (SQLException ex) {
            System.out.println("Error al ejecutar la query: " + query);
            Logger.getLogger(FullTableScan.class.getName()).log(Level.SEVERE, null, ex);
        }
        time_end = System.currentTimeMillis();
        return ("Tiempo de ejecucion " + ( time_end - time_start ) + " milisegundos");
    }
    
    /**
     * Valda que la query tenga todas sus variables seteadas
     * @param query Query a validar
     * @return True si se encuentran todas sus variables seteadas, False y aun no
     * se encuentran seteadas sus variables. Esto se hace para que al ejecutar la query
     * no se produsca un error de ejecucion
     */
    private static boolean validateQuery(String query) {
        Matcher matcher = pattern.matcher(query);
        //Si no encuentra conicidencia todo OK
        if (!matcher.find()) {
            return true;
        }
        return false;
    }
}
