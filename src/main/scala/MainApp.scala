import java.net.{ServerSocket, Socket}
import java.io.{DataInputStream, DataOutputStream, ObjectOutputStream}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.mutable.ArrayBuffer

class Item(val name: String, val price: Int)

object MainApp extends App {
    val serverSocket = new ServerSocket(5555)
    
    // receive connections
    val incomingSocket: Future[Socket] = Future { serverSocket.accept() }

    val items: ArrayBuffer[Item] = ArrayBuffer(
        new Item("Horseraddish", 420),
        new Item("Eggplant", 380),
        new Item("Lentil", 750),
        new Item("Potato", 169),
    )

    def processSocket(socket: Socket) {
        val nextIncomingSocket: Future[Socket] = Future { serverSocket.accept() }
        nextIncomingSocket.foreach(socket => {
            processSocket(socket)
        })

        val in = socket.getInputStream()
        val out = socket.getOutputStream()

        val dis = new DataInputStream(in)
        val dos = new DataOutputStream(out)
        val oos = new ObjectOutputStream(out)


        // get array of item names
        val itemNames: ArrayBuffer[String] = ArrayBuffer[String]()
        for (item <- items) {
            itemNames.append(item.name)
        }

        // send array of item names
        oos.writeObject(itemNames)

        // data is the item name
        val data = dis.readLine()
        println(s"Received data from client: ${data}")

        // get price
        var price = "Item does not exist."
        if (itemNames.contains(data)) {
            price = items.filter(_.name==data)(0).price.toString
        }

        // send price
        dos.writeBytes(price)
        // for some reason flush does not send this output stream
        // dos.flush()
        // but close does
        dos.close()
    }

    incomingSocket.foreach(socket => {
            processSocket(socket)
        })

    // initialise input
    var input: String = ""
    // add item or terminate server
    do {
        input = scala.io.StdIn.readLine("Enter item NAME to add item (or 'q' to terminate server): \n")
        if (input != "q") {
            val price: String = scala.io.StdIn.readLine("Enter item PRICE in cents (or press Enter to cancel): \n")
            try {
                items.append(new Item(input, price.toInt))
            } catch {
                case e: Exception => println("Item price must be integer.")
            }
            println("")
        }
        
    } while(input != "q")

}
