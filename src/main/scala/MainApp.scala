import java.net.{ServerSocket, Socket}
import java.io.{DataInputStream, DataOutputStream}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MainApp extends App {
    val serverSocket = new ServerSocket(5555)
    
    // receive connections
    val incomingSocket: Future[Socket] = Future { serverSocket.accept() }

    def processSocket(socket: Socket) {
        val nextIncomingSocket: Future[Socket] = Future { serverSocket.accept() }
        nextIncomingSocket.foreach(socket => {
            processSocket(socket)
        })

        val instream = socket.getInputStream()
        val outstream = socket.getOutputStream()

        val dis = new DataInputStream(instream)
        val dos = new DataOutputStream(outstream)

        val data = dis.readLine()
        println(s"Received data from client: ${data}")
        dos.writeBytes(s"Hello, ${data}\n")

    }

    incomingSocket.foreach(socket => {
            processSocket(socket)
        })

    scala.io.StdIn.readLine("Press Enter key to terminate server")
}
