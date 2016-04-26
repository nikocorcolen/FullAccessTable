/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fulltablescan;

/**
 *
 * @author CDIAZ
 */
public class Report {

    private String execTime;
    private String query;
    private boolean fullAccess;
    
    public Report() {
    }

    public String getExecTime() {
        return execTime;
    }

    public void setExecTime(String execTime) {
        this.execTime = execTime;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isFullAccess() {
        return fullAccess;
    }

    public void setFullAccess(boolean fullAccess) {
        this.fullAccess = fullAccess;
    }
}
