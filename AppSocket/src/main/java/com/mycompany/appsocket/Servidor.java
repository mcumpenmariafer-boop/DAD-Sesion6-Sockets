/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.appsocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class Servidor extends Conexion {
    public Servidor() throws IOException { super("servidor"); } //Se usa el constructor para servidor de Conexion

    public void startServer() //Metodo para iniciar el servidor
    {
        try
        {
            System.out.println("Esperando..."); //Esperando conexion
            cs = ss.accept(); //Accept comienza el socket y espera una conexion desde un cliente
            System.out.println("Cliente en linea");
            //Se obtiene el flujo de salida del cliente para enviarle mensajes
            salidaCliente = new DataOutputStream(cs.getOutputStream());
            //Se le envia un mensaje al cliente usando su flujo de salida
            salidaCliente.writeUTF("Peticion recibida y aceptada");
            //Se obtiene el flujo entrante desde el cliente
            DataInputStream entrada = new DataInputStream(cs.getInputStream());
            try {
                while (true) //Mientras haya mensajes desde el cliente
                {
                    mensajeServidor = entrada.readUTF();
                    System.out.println(mensajeServidor); //Se muestra el mensaje recibido
                }
            } catch (EOFException e) {
                //El cliente cerro la conexion
            }
            System.out.println("Fin de la conexion");
            ss.close(); //Se finaliza la conexion con el cliente
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}