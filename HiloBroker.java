import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.text.NumberFormat;

/**
 * Este es un hilo para sacar las operaciones que ha hecho cada broker.
 */
public class HiloBroker extends Thread{
    String broker;
    String anyo, mes, dia;
    
    public HiloBroker(String broker, String anyo, String mes, String dia){
        this.broker=broker;
        this.anyo=anyo;
        this.mes=mes;
        this.dia=dia;
    }
    
    public void run(){
        try{
            //Este es el link "tipo" de visualeconomy de donde sacaremos los datos de compras/ventas de los brokers
            String link = "http://www.visualeconomy.com/MarketMonitor/MarketMonitor.aspx?Page=MMO_NEG_BROK_AG_CV&SYMBOL1="+broker+"&PTPP=ECO_HOME;MMO_EUR&APP=ECO";
            //Remplazamos del link anterior el espacio que tiene el ticker del broker (brokers[i]) por un %20 para que lo reconozca el navegador.
            String web = getWeb(link.replace(" ","%20"), 240);
            
            //Cada broker va a tener su carpeta en la que vamos a ir creando carpetas para cada anyo/mes y dentro de la cual habra un archivo .txt para cada dia.
            File dir = new File("Historicos\\Brokers\\"+broker+"\\"+anyo+"\\"+mes);
            dir.mkdirs(); //creamos las carpetas correspondientes.
            //Creamos el archivo donde guardaremos los datos del dia de hoy.
            File f = new File(dir, broker+"-"+anyo+mes+dia+".txt");
            PrintWriter pw = new PrintWriter(f);
            //Esribimos una sola vez por archivo la leyenda de cada columna.
            pw.println("Valor\tCompra\tVenta\tC-V");
            
            //Este es el patron para buscar el nombre de las acciones que ha comprado/vendido el broker.
            Pattern accion = Pattern.compile("rel=\"nofollow\">(.*?)</a><input");
            Matcher m = accion.matcher(web);            
            
            while(m.find()) {
                //Este es patron para ver cuantos titulos (volumenes) ha comprado/vendido el broker sobre la accion que estemos analizando (sacada con el matcher m).
                Pattern volumenes = Pattern.compile("</a></td>\n.*<td class=\"CellNegBrokerValue\">(.*?)</td>\n.*<td class=\"CellNegBrokerValue\">(.*?)</td>\n.*</tr>");
                //Si tambien quiero sacar los TOP 5 de compras y ventas el pattern tendria que ser el siguiente (es igual pero sin la primera parte):
                //Pattern volumenes = Pattern.compile("</a></td>\n.*<td class=\"CellNegBrokerValue\">(.*?)</td>\n.*<td class=\"CellNegBrokerValue\">(.*?)</td>\n.*</tr>");
                Matcher m2 = volumenes.matcher(web);
                
                if(m2.find(m.end())){                    
                    int compras = Integer.parseInt(m2.group(1).replace(".",""));
                    int ventas = Integer.parseInt(m2.group(2).replace(".",""));
                    int neto = compras-ventas;
                    pw.println(m.group(1)+"\t"+compras+"\t"+ventas+"\t"+neto);
                }
            }
            pw.close();
        }catch(Exception e) {System.err.println("Ha habido un error al sacar los datos del broker: "+broker);}
        long tiempoFin = System.currentTimeMillis();
        System.out.println("("+broker+") Tiempo de finalizacion en miliseg: "+tiempoFin);
    }
    
    /**
     * Le pasamos como parametro la direccion de la web de la que queremos sacar su codigo fuente, 
     * y el numero de lineas que queremos ignorar de dicho codigo fuente.
     */
    public static String getWeb(String direccion, int x) throws Exception {
        URL pagina = new URL(direccion);
        BufferedReader in = new BufferedReader(new InputStreamReader(pagina.openStream()));
        
        //Pasamos las X primera lineas, ya que no contienen nada de información
        for(int i = 0; i < x; i++) in.readLine(); 
        
        String entrada;
        String resultado="";
        while ((entrada = in.readLine()) != null)
            resultado+=entrada+"\n";
            
        in.close();
        return resultado;
    }
}
