package org.seshat;
import org.seshat.util.DatabaseConnection;
import java.sql.Connection;

public class Main {
    public static void main(String[] args){
        try{
            Connection con = DatabaseConnection.getConnection();
            System.out.println("Conexión Exitosa!");
            con.close();
        } catch (Exception e){
            System.out.println("Error :" + e.getMessage());
        }
    }
}