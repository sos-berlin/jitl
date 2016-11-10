package com.sos.jitl.reporting.db;

public class Test {

 
        public static void main(String args[]) {
            String Str = new String("1/2/3");

            System.out.print("Return Value :" );
            System.out.println(Str.replaceFirst("^/",""));

            Str = new String("/1/2/3");

            System.out.print("Return Value :" );
            System.out.println(Str.replaceFirst("^/",""));
         }
     

}
