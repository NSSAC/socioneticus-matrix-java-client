package org.bii.nssac.matrix_client;

import com.google.gson.Gson;
import org.bii.nssac.matrix_client.BagOfPrimitives;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        BagOfPrimitives obj = new BagOfPrimitives();
        Gson gson = new Gson();
        String json = gson.toJson(obj);

        System.out.println(json);
    }
}
