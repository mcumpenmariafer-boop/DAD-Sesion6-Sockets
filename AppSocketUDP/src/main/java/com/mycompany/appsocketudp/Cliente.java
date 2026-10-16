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
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cliente {
    public static void main(String[] args) {
        //puerto del servidor
        final int PUERTO_SERVIDOR = 5000;
        //buffer donde se almacenara los mensajes
        byte[] buffer = new byte[1024];
        try {
            //Obtengo la localizacion de localhost
            InetAddress direccionServidor = InetAddress.getByName("localhost");
            //Creo el socket de UDP
            DatagramSocket socketUDP = new DatagramSocket();
            String mensaje = "Saludos desde el cliente!";
            //Convierto el mensaje a bytes
            buffer = mensaje.getBytes();
            //Creo un datagrama
            DatagramPacket pregunta = new DatagramPacket(buffer, buffer.length, direccionServidor, PUERTO_SERVIDOR);
            //Lo envio con send
            System.out.println("Envio el datagrama");
            socketUDP.send(pregunta);
            //Preparo la respuesta
            byte[] bufferRespuesta = new byte[1024];
            DatagramPacket peticion = new DatagramPacket(bufferRespuesta, bufferRespuesta.length);
            //Recibo la respuesta
            socketUDP.receive(peticion);
            System.out.println("Recibo la peticion");
            //Cojo los datos y los muestro
            mensaje = new String(peticion.getData(), 0, peticion.getLength());
            System.out.println(mensaje);
            //Cierro el socket
            socketUDP.close();
        } catch (SocketException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}