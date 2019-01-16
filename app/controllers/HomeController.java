package controllers;

import actors.RoomActor;
import actors.WebSocketActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import messages.Login;
import models.User;
import Reference.UserRef;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class HomeController extends Controller {
    private final ActorSystem system;
    private final Materializer materializer;
    private final ActorRef lobbyActor;

    @Inject
    public HomeController(ActorSystem system, Materializer materializer,@Named("LobbyActor") ActorRef lobbyActor) {
        this.materializer = materializer;
        this.system = system;
        this.lobbyActor = lobbyActor;
    }

    public Result index() {
        return ok(views.html.index.render(RoomActor.tick));
    }

    public Result tick() {
        RoomActor.tick = Integer.parseInt(request().body().asFormUrlEncoded().get("tick")[0]);
        return redirect(controllers.routes.HomeController.index());
    }

    public WebSocket ws() {
        long id = request().asScala().id();

        return WebSocket.Json.accept(request ->
                ActorFlow.actorRef(out ->{
                    UserRef userRef = new UserRef(new User(id), out);
                    Login login = new Login(userRef);
                    lobbyActor.tell(login, ActorRef.noSender());
                    return WebSocketActor.props(userRef, lobbyActor);
                }, system, materializer)
        );
    }
}
