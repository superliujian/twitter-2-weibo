package com.rakutec.weibo;

import com.rakutec.weibo.utils.HttpServletRouter;
import com.rakutec.weibo.utils.T2WUser;
import org.apache.log4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.RequestToken;
import weibo4j.Weibo;
import weibo4j.WeiboException;
import weibo4j.http.AccessToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author Rakuraku Jyo
 */
public class CallbackServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CallbackServlet.class.getName());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpServletRouter r = new HttpServletRouter(request);
        r.setPattern("/:type");

        if (request.getParameter("denied") != null) {
            response.sendRedirect("/");
            return;
        }

        HttpSession session = request.getSession(false);
        String login_user = (String) session.getAttribute("login_user");
        String token = (String) session.getAttribute("token");
        String tokenSecret = (String) session.getAttribute("tokenSecret");
        String oauthVerifier = request.getParameter("oauth_verifier");

        String server = "http://" + request.getServerName();

        if (r.is(":type", "weibo")) {
            try {
                Weibo weibo = new Weibo();

                AccessToken accessToken = weibo.getOAuthAccessToken(token, tokenSecret, oauthVerifier);
                if (accessToken != null) {
                    T2WUser tid = T2WUser.findOneByUser(login_user);
                    tid.setToken(accessToken.getToken());
                    tid.setTokenSecret(accessToken.getTokenSecret());
                    tid.save();

                    if (!tid.ready()) { // send for the first time
                        weibo.updateStatus("Weibo, say hello to Twitter. From T2W Sync " + server + " #t2w_sync#");
                    }
                } else {
                    log.error("Can't auth " + login_user + " for Weibo. " + request.getQueryString());
                }
            } catch (WeiboException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        } else if (r.is(":type", "twitter")) {
            try {
                TwitterFactory factory = new TwitterFactory();
                Twitter t = factory.getInstance();

                twitter4j.auth.RequestToken req = (RequestToken) session.getAttribute("requestToken");
                twitter4j.auth.AccessToken accessToken = t.getOAuthAccessToken(req, oauthVerifier);
                session.removeAttribute("requestToken");

                if (accessToken != null) {
                    t.setOAuthAccessToken(accessToken);
                    User user = t.verifyCredentials();
                    login_user = user.getScreenName();

                    T2WUser tid = T2WUser.findOneByUser(login_user);
                    tid.setTwitterToken(accessToken.getToken());
                    tid.setTwitterTokenSecret(accessToken.getTokenSecret());
                    tid.save();
                    
                    if (!tid.ready()) { // send for the first time
                        t.updateStatus("Twitter, say hello to Weibo. From T2W Sync " + server + " #t2w_sync");
                    }

                    session.setAttribute("login_user", login_user);
                }
            } catch (TwitterException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
        }
        response.sendRedirect("/u/" + login_user);
    }
}