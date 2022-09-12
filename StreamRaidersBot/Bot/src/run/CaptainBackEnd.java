package run;

import include.Http.NoConnectionException;
import srlib.SRR;
import srlib.SRR.NotAuthorizedException;

public class CaptainBackEnd extends AbstractBackEnd<CaptainBackEnd> {

	public CaptainBackEnd(String cid, SRR req) {
		super(cid, req);
	}

	@Override
	void ini() throws NoConnectionException, NotAuthorizedException {
		// TODO Auto-generated method stub
	}

}
