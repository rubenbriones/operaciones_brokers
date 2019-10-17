import java.util.*;
import java.io.File;
import java.util.regex.*;

/**
 * 
 */
public class AnalisisDeDatos{
    public static void main(String[] args)throws Exception{
        //El mes 0 es enero.
        Calendar ini = new GregorianCalendar(2015,5-1,29);
        Calendar fin = new GregorianCalendar(2015,5-1,31);
        recogerTodasLasOperaciones("CARBURES",ini,fin,0); //el nombre de la accion hay que ponerlo como aparece en VisualEconomy
        //recogerTodasLasOperaciones("NOR BI","NATRA",ini,fin,0); //el nombre de la accion hay que ponerlo como aparece en VisualEconomy
    }
        
    /**
     * Saca todas las operaciones de TODOS los brokers, sobre una accion concreta.
     * @accion Hay que pasarle el nombre de la accion tal y como aparece en VisualEconomy.
     * si vale 0 es que saca TODAS las operaciones.
     * Si vale 1 solo saca las operaciones COMPRADORAS.
     * Si vale -1 solo scaa las operaciones VENDEDORAS.
     */
    public static void recogerTodasLasOperaciones(String accion, Calendar fechaIni, Calendar fechaFin, int tipo) throws Exception{
        int anyoIni = fechaIni.get(Calendar.YEAR);
        int mesIni = fechaIni.get(Calendar.MONTH)+1;
        int diaIni = fechaIni.get(Calendar.DAY_OF_MONTH);
        
        int anyoFin = fechaFin.get(Calendar.YEAR);
        int mesFin = fechaFin.get(Calendar.MONTH)+1;        
        int diaFin = fechaFin.get(Calendar.DAY_OF_MONTH);
        
        File dir = new File("Historicos\\Brokers");
        File[] brokers = dir.listFiles();
        for(int i=0; i<brokers.length; i++){
            
            File[] anyos = brokers[i].listFiles();
            for(int j=0; j<anyos.length; j++){
                int anyo= Integer.parseInt(anyos[j].getName());
                if(anyo>=anyoIni && anyo<=anyoFin){
                    
                    File[] meses = anyos[j].listFiles();
                    for(int k=0; k<meses.length; k++){
                        int mes= Integer.parseInt(meses[k].getName());
                        
                        Calendar fec = new GregorianCalendar(anyo, mes-1, 1);
                        if(fec.compareTo(fechaFin)<=0){
                            //if(anyo>anyoIni || (mes>=mesIni && mes<=mesFin)){
                            
                            File[] dias = meses[k].listFiles();
                            for(int m=0; m<dias.length; m++){
                                String nombre = dias[m].getName();
                                String diaStr = nombre.substring(nombre.length()-6, nombre.length()-4);
                                int dia = Integer.parseInt(diaStr);
    
                                fec = new GregorianCalendar(anyo, mes-1, dia);
                                if(fec.compareTo(fechaIni)>=0 && fec.compareTo(fechaFin)<=0){
                                    //if(anyo>anyoIni || mes>mesIni || (dia>=diaIni && dia<=diaFin)){
                                
                                    Scanner s = new Scanner(dias[m]);
                                    String contenido = "";
                                    while(s.hasNextLine()) contenido += s.nextLine()+"\n";
                                    
                                    Pattern p = Pattern.compile("\n"+accion+"\t(\\d*?)\t(\\d*?)\t(.*?)\n");
                                    Matcher match = p.matcher(contenido);            
                                    if(match.find()){
                                        String st = match.group(3);
                                        int neto = Integer.parseInt(st);
                                        if(tipo==0 || (tipo==1 && neto>0) || (tipo==-1 && neto<0)){
                                            System.out.print(dias[m].getName()+"\t"+match.group());
                                        }
                                    }
                                    s.close();
                                }
                            }
                        }
                    }
                    
                }
            }
        }
        
    }
    
    /**
     * Saca todas las operaciones de un broker concreto, sobre una accion concreta.
     * @tickerBroker Hay que pasarle el ticker del broker
     * @accion Hay que pasarle el nombre de la accion tal y como aparece en VisualEconomy.
     * La variable "tipo":
     * si vale 0 es que saca TODAS las operaciones.
     * Si vale 1 solo saca las operaciones COMPRADORAS.
     * Si vale -1 solo scaa las operaciones VENDEDORAS.
     */
    public static void recogerTodasLasOperaciones(String tickerBroker, String accion, Calendar fechaIni, Calendar fechaFin, int tipo) throws Exception{
        int anyoIni = fechaIni.get(Calendar.YEAR);
        int mesIni = fechaIni.get(Calendar.MONTH)+1;
        int diaIni = fechaIni.get(Calendar.DAY_OF_MONTH);
        
        int anyoFin = fechaFin.get(Calendar.YEAR);
        int mesFin = fechaFin.get(Calendar.MONTH)+1;        
        int diaFin = fechaFin.get(Calendar.DAY_OF_MONTH);
        
        File dir = new File("Historicos\\Brokers\\"+tickerBroker);
        
        File[] anyos = dir.listFiles();
        for(int j=0; j<anyos.length; j++){
            int anyo= Integer.parseInt(anyos[j].getName());
            if(anyo>=anyoIni && anyo<=anyoFin){
                
                File[] meses = anyos[j].listFiles();
                for(int k=0; k<meses.length; k++){
                    int mes= Integer.parseInt(meses[k].getName());
                    
                    Calendar fec = new GregorianCalendar(anyo, mes-1, 1);
                    if(fec.compareTo(fechaFin)<=0){
                        //if(anyo>anyoIni || (mes>=mesIni && mes<=mesFin)){
                        
                        File[] dias = meses[k].listFiles();
                        for(int m=0; m<dias.length; m++){
                            String nombre = dias[m].getName();
                            String diaStr = nombre.substring(nombre.length()-6, nombre.length()-4);
                            int dia = Integer.parseInt(diaStr);

                            fec = new GregorianCalendar(anyo, mes-1, dia);
                            if(fec.compareTo(fechaIni)>=0 && fec.compareTo(fechaFin)<=0){
                                //if(anyo>anyoIni || mes>mesIni || (dia>=diaIni && dia<=diaFin)){
                            
                                Scanner s = new Scanner(dias[m]);
                                String contenido = "";
                                while(s.hasNextLine()) contenido += s.nextLine()+"\n";
                                
                                Pattern p = Pattern.compile("\n"+accion+"\t(\\d*?)\t(\\d*?)\t(.*?)\n");
                                Matcher match = p.matcher(contenido);            
                                if(match.find()){
                                    String st = match.group(3);
                                    int neto = Integer.parseInt(st);
                                    if(tipo==0 || (tipo==1 && neto>0) || (tipo==-1 && neto<0)){
                                        System.out.print(dias[m].getName()+"\t"+match.group());
                                    }
                                }
                                s.close();
                            }
                        }
                    }
                }
                
            }
        }
                
    }
}
