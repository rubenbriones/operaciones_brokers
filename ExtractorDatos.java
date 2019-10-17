import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.text.NumberFormat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 
 */
public class ExtractorDatos{
    /**
     * Extraemos cada dia/hora/etc las compras y ventas de todos los brokers. Del siguiente link:
     * http://www.visualeconomy.com/MarketMonitor/MarketMonitor.aspx?Page=MMO_NEG_BROK_AG_CV&SYMBOL1=ATE%20MA&PTPP=ECO_HOME;MMO_EUR&APP=ECO
     * Hay que pasarle como parametro un array con los TICKETS de todos los brokers.
     */
    public static void visualEconomy(String[] brokers)throws Exception{
        //Sacamos los datos de la feca actual para nombrar las carpetas donde meteremos los movimientos de cada broker en el dia de hoy.
        Calendar c = Calendar.getInstance();
        
        //Comprobamos si es mas de medianoche, para  que si es asi restar un dia a la fecha actual, 
        //para que las carpetas se creen con la fecha de ayer que es de cuando son los datos.
        int hora = c.get(Calendar.HOUR_OF_DAY);
        if(hora<9) c.add(Calendar.DATE, -1);
        //Y ahora comprobamos si son mas de las 18:00 y si no es asi paramos el programa, pues los datos puede que no esten actualizados todavia.
        if(hora>=9 && hora<17){
            System.out.println("TODAVIA NO SON LAS 18:00 Y ES MUY PROBABLE QUE LOS DATOS NO ESTEN ACTUALIZADOS (tener en cuenta el delay de 15m)");
            System.out.println("ASI QUE EL PROGRAMA VA A PARARSE Y NO VA A HACER NADA. RECUERDA EJECUTARLO CUANDO YA SEAN LAS 18:00");
            System.exit(0);
        }
        
        String dia = String.format("%02d",c.get(Calendar.DATE));
        String mes = String.format("%02d",c.get(Calendar.MONTH)+1);
        String anyo = Integer.toString(c.get(Calendar.YEAR));
        
        ExecutorService exec = Executors.newFixedThreadPool(15);
        for(int i=0; i<brokers.length; i++){
            //Creamos un hilo para cada broker, y asi aumentamos la eficiencia un monton. Tarda 9 veces menos si lo hacemos asi, que si lo hacemos secuencialmente.
            HiloBroker h = new HiloBroker(brokers[i],anyo,mes,dia);
            exec.execute(h);
        }
        
        exec.shutdown();
        //ESTO HAE QUE EL PROGRAMA SOLO ACABE CUANDO HAYAN TERMINADO TODOS LOS HILOS O CUANDO SE PASE EL TIMEOUT.
        try {
            boolean b = exec.awaitTermination(120, TimeUnit.SECONDS);
            System.out.println("LOS DATOS DE TODOS LOS BROKERS SE HAN DESCARGADO: " + b);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
                
    /**
     * Extraemos cada dia/hora/etc las cotizaciones de todas las acciones del MERCADO COTINUO(con 15 minutos de retraso). De la web:
     * http://www.bolsamadrid.es/esp/aspx/Mercados/Precios.aspx
     */
    public static void bolsaMadrid()throws Exception{
        //Posibles links para un futuro si no fuera porque en el del MC no aparecen todas las acciones
        //http://www.bolsamadrid.es/esp/aspx/Mercados/Precios.aspx?mercado=MC
        //http://www.bolsamadrid.es/esp/aspx/Mercados/Precios.aspx?mercado=LT
        //http://www.bolsamadrid.es/esp/aspx/Mercados/Precios.aspx?mercado=MABEE
        
        String[] links = new String[3];
        
        //Link del MERCADO CONTINUO
        links[0] = "http://www.bolsamadrid.es/esp/aspx/Mercados/Precios.aspx?"+
                        "__EVENTTARGET=ctl00%24Contenido%24Todos&"+
                        "__VIEWSTATE=YPeuRs%2FA0USxj8cS4DCaKB%2FGwHqm7HjOnnDjiqqDII2tycLZxyxFBYeHo13WdeQAwMZ2ZKxlHhTdLXfmo4mgIOtCKiOoMrhzigra2APZLy7XdDHVoQIj9pU1MEeuvNGRAN%2FP2mIAALY4gdZ4LW0v5YyWJfZVR0BQPcWoNFMn1htCUfyBknLS34mOhI%2Bu1E31jX0DFn883HWmETSt8apD0IzCUPJ%2F8tHKlVbeXQxpSK26Ha7maCpNqUZBp7Af3NQz6rEx1w%3D%3D&"+
                        "__EVENTVALIDATION=4ogBbRp%2FuxmWWf4ojEKKKHdmocLjTEN6f0%2BNEH9A8IoX8TDCGExxYN3c1htowQ2ovkPSV6iuN4WLDfG3XX8np5jzEAhDXo384W7V6czdg6ra3DRZ675TTiuZIYyWHGOABULmxJw57OfGrx5BFMJYNtbuEZ3uRmG8rAzideiNRljBced6lsUJQ0WkeCdpR5R9Ky2%2FHhOWs%2B0A2Zul4us9uSViUtk8U%2BWSSzqZrU%2FIk6EjNvkSSVClXamoWZHgshntQIwT1XvXyjk%2FtVAMhWYzz73SNfNlKiZ%2B3rooiV2tNCgmWjJWJsOzT%2F1EEAgzDFK82zePBgC%2FffZoGIF9SUPBzOTLEYI%3D&"+
                        "ctl00%24Contenido%24SelMercado=MC&"+
                        "ctl00%24Contenido%24Sel%C3%8Dndice=&"+
                        "ctl00%24Contenido%24SelSector=";
        
        //Link del LATIBEX
        links[1] = "http://www.bolsamadrid.es/esp/aspx/Mercados/Precios.aspx?"+
                       "__VIEWSTATE=brsb139hctubQ1dHYGzzapjdO6odSlRk%2BlUxOhlNFs4sMO4TqcUFkhqqZu0uxKWxpWf1j8zemnCi0mAcYKcNYx%2F7eSqP%2BzPGJUeskKCw5oRg9Ii4DHlBWmJJkEmaEcrx0ijHX9UGS8kKcdQrDSOZysYRN8SpY0YtT0QuUEqDyP6VR9aA0DZPkXkvZrqvAgveJOo5K%2Bz4pOKRMFoyeWcYQYTYDMI%3D&"+
                       "__EVENTVALIDATION=sjvDx24EiTxTeyfc11f9X95%2F1yEodiIf314b4HtYZIvCcKO8woLhK9a8yDzyvqXrtpTWTQRn6lbU%2F1HOpQCtc5OmVpLEhMl6B8orTSRR9Y3FT7Ml8TmkBLhFT6mW4D3BMXYdZcW3rwle8hQY7l8xQEc9wg4mGV0UP4Hn2O2kB42S%2FgYqitNXkAjoWua%2Fl2nMEWKbURR6W%2B8f4s4jpQwpx3zAloeVFlColdK0HpuwHvxgsqPUoAsIg0uaC0fBqcwIi%2BBg5Kd%2FAc4W1JvpjkqR5JFr7FefAsCPdIDFBZU1QBYSfXLd9nw2B%2FyAzzQEbv69vlrOdg%3D%3D&"+
                       "ctl00%24Contenido%24SelMercado=LT&"+
                       "ctl00%24Contenido%24Sel%C3%8Dndice=&"+
                       "ctl00%24Contenido%24SelSector=&"+
                       "ctl00%24Contenido%24Consultar=Consultar";
        
        //Link del MAB
        links[2] = "http://www.bolsamadrid.es/esp/aspx/Mercados/Precios.aspx?"+
                       "__VIEWSTATE=4pS5rHEutM8iaEdKm9Wc7a3qBzms9mZNvtld5W4T%2BDTle4yuqAolhNldy5CP20QrCalR%2FFFl3c%2BuvD6TPo1IkeyuHs%2B7uJyAhXiP3ifCDwGC6zavzG2Sd0u9fThP3HUoe9iPs%2F%2FAad%2FdB043LeUuBcoSEJtrjLXQBY3Fsap2aYIT7AuDrfCxcYz1dOlwibFTzegP37yUvRjfYTJMSj0SZvvuqQhGeDVw%2FheC7gXfrthGrfMX5MPuZlzHOEL98cw6JKYGeQ%3D%3D&"+
                       "__EVENTVALIDATION=2h8mOoq4zRWwgt3pPOqruYnU38m16tok8x3suFRMqwkm8EGDeQtuFgT5XlzNEXtVuzlvqwUBuSZZxJnGg0DIlytqMyYVsJMwri5VEqKEx8daebiRT%2FlegYtQrxGn%2FiAlJRJzykTyo9Lx346KqRLTsIJ5yle4oYLdeea7jb9YT6Eti2Q1LkTaH8TEsXznjF1xvDluIA1EFN67MvicHY1xDHmOrnvS%2BzVn%2FpQ7kfTy3w1Hs6g7NXr3Q%2Fgzo%2BXrc1oH1lfF948r01ep7tEcQq1lc3Ejxo2S0ubfUuJTCXUhCH4XMGz3xRQUcvCD4w4uWPgFsc%2FgYg%3D%3D&"+
                       "ctl00%24Contenido%24SelMercado=MABEE&"+
                       "ctl00%24Contenido%24Sel%C3%8Dndice=&"+
                       "ctl00%24Contenido%24SelSector=&"+
                       "ctl00%24Contenido%24Consultar=Consultar";
        
        for(int i=0; i<links.length; i++){
            String web = getWeb(links[i],0);
            
            Pattern cotizacion = Pattern.compile("ISIN=(.*?)\">(.*?)</a></td><td>(.*?)</td><td class=\".*\">(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td align=\"center\">(.*?)</td><td class=\"Ult\" align=\"center\">(.*?)</td>");
            Matcher m = cotizacion.matcher(web);
            
            String accionAnt=""; //esta string la uso porque hay 4 acciones del MAB(ebioss, ibercom, suavitas, catenon) que estan repertidas.
            while(m.find()) {
                //Reemplazo los "." y las "/" porque no se admiten en los nombres de los archivos.
                String accion = m.group(2).replace(".","").replace("/","");
                
                if(!accion.equals(accionAnt)){ //Comprobamos que la accion a anyadir no la hallamos anyadido antes.
                    accionAnt=accion;
                    File f = new File("Historicos\\Acciones\\"+accion+".txt");
                    FileWriter pw = new FileWriter(f,true);
                    
                    //Si es la primera vez que creamos el fichero para esa accion escribimos el significado de las columnas en la primera linea.
                    //A partir de la primera vez puedo quitar esta comprobacion y me ahorraria TIEMPO.
                    if(!f.exists()){f.mkdir(); pw.write("Nombre\tUltimo\tVar%\tMaximo\tMinimo\tVolumen\tEfectivo(miles€)\tFecha\tHora");}
                    
                    //Escribimos en el archivo los 9 datos que sacamos de cada Accion (el elemento m.group(1) es el ISIN que no usamos de momento)
                    for(int j=2; j<=10; j++){
                        pw.write(m.group(j)+"\t");
                    }
                    pw.write("\r\n");
                    pw.close();
                }
            }
        }
        
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
