package run.captain;

import include.Http.NoConnectionException;
import run.AbstractBackEnd;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;

public class CaptainBackEnd extends AbstractBackEnd<CaptainBackEnd> {

	public CaptainBackEnd(String cid, SRR req) {
		super(cid, req);
	}

	@Override
	protected void ini() throws NoConnectionException, NotAuthorizedException {
		// TODO Auto-generated method stub
	}

}
