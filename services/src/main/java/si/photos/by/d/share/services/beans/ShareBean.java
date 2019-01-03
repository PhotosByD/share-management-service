package si.photos.by.d.share.services.beans;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import si.photos.by.d.share.models.dtos.Photo;
import si.photos.by.d.share.models.entities.Share;
import si.photos.by.d.share.services.configuration.AppProperties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RequestScoped
public class ShareBean {
    private Logger log = Logger.getLogger(ShareBean.class.getName());

    @Inject
    private EntityManager em;

    @Inject
    private AppProperties appProperties;

    private Client httpClient;

    @Inject
    @DiscoverService("photo-management-service")
    private Optional<String> photoUrl;

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

    public List<Photo> getSharedPhotosForUser(Integer id) {
        List<Photo> result = new ArrayList<>();
        TypedQuery<Share> query = em.createQuery("SELECT s FROM share s WHERE s.userId = :id", Share.class);
        query.setParameter("id", id);

        List<Share> sharedPhotos = query.getResultList();
        if (sharedPhotos.isEmpty()) {
            throw new NotFoundException();
        }

        for (Share s : sharedPhotos){
            result.add(getPhoto(s.getPhotoId()));
        }
        return result;
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

    private Photo getPhoto(Integer photoId) {
        if (appProperties.isExternalServicesEnabled() && photoUrl.isPresent()) {
            try {
                return httpClient
                        .target(photoUrl.get() + "/v1/photos?where=id:EQ:" + photoId)
                        .request().get(new GenericType<Photo>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }
        }
        return null;
    }
}