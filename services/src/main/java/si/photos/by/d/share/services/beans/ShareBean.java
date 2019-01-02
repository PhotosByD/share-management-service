package si.photos.by.d.share.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import si.photos.by.d.share.models.entities.Share;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class ShareBean {
    private Logger log = Logger.getLogger(ShareBean.class.getName());

    @Inject
    private EntityManager em;

    private Client httpClient;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
    }

    public List<Share> getShares() {
        TypedQuery<Share> query = em.createNamedQuery("Share.getAll", Share.class);
        return query.getResultList();
    }

    public List<Share> getSharesFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, Share.class, queryParameters);
    }

    public Share getShare(Integer shareId) {
        Share share = em.find(Share.class, shareId);

        if (share == null) {
            throw new NotFoundException();
        }

        return share;
    }

    public List<Share> getsharesForUser(Integer id) {
        TypedQuery<Share> query = em.createQuery("SELECT s FROM share s WHERE s.userId = :id", Share.class);
        query.setParameter("id", id);

        return query.getResultList();
    }

    public List<Share> getSharesForPhoto(Integer id) {
        TypedQuery<Share> query = em.createQuery("SELECT s FROM share s WHERE s.photoId = :id", Share.class);
        query.setParameter("id", id);

        return query.getResultList();
    }

    public Share createShare(Share share) {
        try {
            beginTx();
            em.persist(share);
            commitTx();
        } catch (Exception e) {
            log.warning("There was a problem with saving new share for photo " + share.getPhotoId());
            rollbackTx();
        }
        log.info("Successfully saved new share for photo" + share.getPhotoId());
        return share;
    }

    public Share updateShare(Integer shareId, Share share) {
        Share s = em.find(Share.class, shareId);

        if (s == null) return null;

        try {
            beginTx();
            share.setId(shareId);
            em.merge(share);
            commitTx();
        } catch (Exception e) {
            log.warning("There was a problem with updating share for photo " + share.getPhotoId());
            rollbackTx();
        }
        log.info("Successfully updated share for photo " + share.getPhotoId());
        return share;
    }

    public boolean deleteShare(Integer shareId) {
        Share share = em.find(Share.class, shareId);

        if (share != null) {
            try {
                beginTx();
                em.remove(share);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else {
            return false;
        }

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }
}