/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.appsocketudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor {
    public static void main(String[] args) {
        final int PUERTO = 5000;
        try {
            System.out.println("Iniciado el servidor UDP");
            //Creacion del socket
            DatagramSocket socketUDP = new DatagramSocket(PUERTO);
            //Siempre atendera peticiones
            while (true) {
                byte[] buffer = new byte[1024];
                //Preparo la respuesta
                DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
                //Recibo el datagrama
                socketUDP.receive(peticion);
                System.out.println("\nRecibo la informacion del cliente");
                //Convierto lo recibido y muestro el mensaje
                String mensaje = new String(peticion.getData(), 0, peticion.getLength());
                System.out.println(mensaje);
                //Obtengo el puerto y la direccion de origen
                int puertoCliente = peticion.getPort();
                InetAddress direccion = peticion.getAddress();
                mensaje = "Saludos desde el servidor!";
                buffer = mensaje.getBytes();
                //Creo el datagrama
                DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length, direccion, puertoCliente);
                //Envio la informacion
                System.out.println("Envio la informacion del cliente");
                socketUDP.send(respuesta);
            }
        } catch (SocketException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}