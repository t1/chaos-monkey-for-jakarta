package de.codecentric.chaosmonkey.jakarta.ui;

import com.github.t1.bulmajava.basic.Body;
import com.github.t1.bulmajava.basic.Html;
import com.github.t1.bulmajava.basic.Renderable;
import com.github.t1.bulmajava.basic.Renderer;
import com.github.t1.bulmajava.elements.Title;
import com.github.t1.bulmajava.layout.Section;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static com.github.t1.bulmajava.basic.Html.html;
import static com.github.t1.bulmajava.layout.Section.section;
import static de.codecentric.chaosmonkey.jakarta.ui.Application.CHAOS_UI_ROOT;

@RequestScoped
@RequiredArgsConstructor(onConstructor_ = @Inject) @NoArgsConstructor(force = true)
public class Page implements Renderable {
    private final @NonNull HttpSession session;

    @ConfigProperty(name = "htmx.debug", defaultValue = "false") boolean debug;

    private Html html;
    private Body body;
    private Section section;

    public Page title(String title) {
        this.html = html(title)
                .stylesheet(CHAOS_UI_ROOT + "/webjars/fortawesome__fontawesome-free/css/all.css")
                .stylesheet(CHAOS_UI_ROOT + "/webjars/bulma/css/bulma.css")
                .script(CHAOS_UI_ROOT + "/webjars/htmx.org/dist/htmx.js")
                .script(CHAOS_UI_ROOT + "/webjars/htmx-ext-json-enc/json-enc.js")
                .script(CHAOS_UI_ROOT + "/webjars/htmx-ext-ws/ws.js")
                .script(CHAOS_UI_ROOT + "/webjars/htmx-ext-debug/debug.js")
                .javaScriptBody(CHAOS_UI_ROOT + "/static/chaos-ui.js")
                .content(this.body = Body.body().content(
                        this.section = section().classes("mt-6")
                                .attr("hx-ext", "ws,json-enc" + (debug ? ",debug" : ""))
                                .attr("ws-connect", "/connect/" + session.getId())
                                .content(Title.title(title))));
        return this;
    }

    public Body body() {return body;}

    public Page content(Renderable... content) {
        this.section.content(content);
        return this;
    }


    @Override public void render(Renderer renderer) {html.render(renderer);}
}
