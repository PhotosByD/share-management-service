package si.photos.by.d.share.api.v1.resources;
import com.kumuluz.ee.logs.cdi.Log;
import si.photos.by.d.share.models.entities.Share;
import si.photos.by.d.share.services.beans.ShareBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Log
@ApplicationScoped
@Path("/shares")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShareResource {
    @Inject
    private ShareBean shareBean;

    @Context
    UriInfo uriInfo;

    @GET
    public Response getShares() {
        List<Share> shares = shareBean.getShares();

        return Response.ok(shares).build();
    }

    @GET
    @Path("/user/{userId}")
    public Response getSharesForUser(@PathParam("userId") Integer userId) {
        List<Share> shares = shareBean.getSharesForUser(userId);
        return Response.ok(shares).build();
    }
    @GET
    @Path("/photo/{photoId}")
    public Response getSharesForPhoto(@PathParam("photoId") Integer photoId) {
        List<Share> shares = shareBean.getSharesForPhoto(photoId);
        return Response.ok(shares).build();
    }

    @GET
    @Path("/filtered")
    public Response getSharesFiltered() {
        List<Share> shares;

        shares = shareBean.getSharesFilter(uriInfo);

        return Response.status(Response.Status.OK).entity(shares).build();
    }

    @GET
    @Path("/{shareId}")
    public Response getShare(@PathParam("shareId") Integer shareId) {
        Share share = shareBean.getShare(shareId);

        if(share == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(share).build();
    }

    @POST
    public Response createShare(Share share) {

        if ( (share.getPhotoId() == null) || (share.getUserId() == null)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            share = shareBean.createShare(share);
        }
        if (share.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(share).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(share).build();
        }
    }

    @PUT
    @Path("{shareId}")
    public Response updateShare(@PathParam("shareId") Integer shareId, Share share) {

        share = shareBean.updateShare(shareId, share);

        if (share == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (share.getId() != null)
                return Response.status(Response.Status.OK).entity(share).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{shareId}")
    public Response deleteShare(@PathParam("shareId") Integer shareId) {

        boolean deleted = shareBean.deleteShare(shareId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
