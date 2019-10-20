package org.bii.nssac.matrix_client;

import org.json.JSONObject;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        JSONObject tomJsonObj = new JSONObject();
        tomJsonObj.put("name", "Tom");
        tomJsonObj.put("birthday", "1940-02-10");
        tomJsonObj.put("age", 76);
        tomJsonObj.put("married", false);

        // Cannot set null directly
        tomJsonObj.put("car", JSONObject.NULL);

        tomJsonObj.put("favorite_foods", new String[] { "cookie", "fish", "chips" });

        // {"id": 100001, "nationality", "American"}
        JSONObject passportJsonObj = new JSONObject();
        passportJsonObj.put("id", 100001);
        passportJsonObj.put("nationality", "American");
        // Value of a key is a JSONObject
        tomJsonObj.put("passport", passportJsonObj);

        if (true) {
            // With four indent spaces
            System.out.println(tomJsonObj.toString(4));
        } else {
            System.out.println(tomJsonObj.toString());
        }
    }
}
