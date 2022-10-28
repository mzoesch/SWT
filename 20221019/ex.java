public class ex {
    
    private void handleIncomingMessage(Object msg) {if(
    msg instanceof EncryptedMessage) msg=((
    EncryptedMessage) msg).decrypt(); if (msg instanceof
    TextMessage) server.broadcast(((TextMessage)
    msg).content); if (msg instanceof AuthMessage){
    AuthMessage authMsg = (AuthMessage) msg; if (server.
    login(this, authMsg.username, authMsg.password)){
    server.broadcast(name + " authenticated.");}else{
    this.send("Login denied.");}}}

}
