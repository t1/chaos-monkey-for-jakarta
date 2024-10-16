package de.codecentric.chaosmonkey.jakarta.ui;

import com.github.t1.bulmajava.basic.Anchor;
import com.github.t1.bulmajava.basic.Element;
import com.github.t1.bulmajava.basic.Renderable;
import com.github.t1.bulmajava.components.Card;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.Map;

import static com.github.t1.bulmajava.basic.Anchor.a;
import static com.github.t1.bulmajava.basic.Basic.i;
import static com.github.t1.bulmajava.basic.Basic.p;
import static com.github.t1.bulmajava.basic.Color.WARNING;
import static com.github.t1.bulmajava.components.Card.card;
import static com.github.t1.bulmajava.elements.Button.button;
import static com.github.t1.bulmajava.form.Input.input;
import static com.github.t1.bulmajava.form.InputType.HIDDEN;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;

@Path(Application.CHAOS_UI_ROOT)
@Produces(TEXT_HTML)
public class Application {
    static final String CHAOS_UI_ROOT = "/chaos-ui";

    @Inject Page page;
    @Inject Messages messages;

    @GET
    public Renderable index() {
        var page = this.page.title(null).content(
                demo(),
                eventStream());
        page.body().onkeyup("Enter", "document.getElementById('submit-demo').click();");
        return page;
    }

    private Card demo() {
        return card()
                .header(p("Demo"))
                .content(p(), // somehow, this is necessary for correct spacing (WTF?)
                        emptyMessages().id("demo-output"))
                .footer(
                        chaosAction("Add 1 Failure", Map.of("statusCode", "503", "failureCount", "1")),
                        chaosAction("Add 4 Failures", Map.of("statusCode", "501", "failureCount", "4")),
                        chaosAction("Add Timeout", Map.of("delay", "5000", "failureCount", "1")),
                        a("Send").id("submit-demo").hasText(WARNING)
                                .attr("hx-get", "/greetings/indirect")
                                .attr("hx-target", "#demo-output")
                                .attr("hx-swap", "innerHTML"));
    }

    private static Anchor chaosAction(String label, Map<String, String> config) {
        return a(label)
                .attr("hx-put", "/chaos/INCOMING/GET/greetings/direct")
                .attr("hx-swap", "none")
                .attr("hx-include", "this")
                .content(config.entrySet().stream().map(e -> input(HIDDEN).name(e.getKey()).value(e.getValue())));
    }

    @DELETE @Path("/messages")
    public Element deleteMessages() {
        messages.clear();
        return emptyMessages();
    }

    private Element emptyMessages() {
        return i("response-goes-here");
    }

    private static Card eventStream() {
        return card()
                .header(p("Event Stream"),
                        button().id("delete-all-messages").ariaLabel("delete all messages").icon("trash")
                                .attr("hx-delete", CHAOS_UI_ROOT + "/messages")
                                .attr("hx-target", "#demo-output")
                                .attr("hx-swap", "innerHTML"))
                .content(p(), // somehow, this is necessary for correct spacing (WTF?)
                        p().id("event-stream"));
    }
}
