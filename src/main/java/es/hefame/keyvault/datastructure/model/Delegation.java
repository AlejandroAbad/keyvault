package es.hefame.keyvault.datastructure.model;

import java.util.Date;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;

public class Delegation implements JsonEncodable {

	private String delegateId;
	private String keypairId;

	private Date notBefore;
	private Date notAfter;

	public Delegation(String delegateId, String keypairId, Date notBefore, Date notAfter) {
		this.delegateId = delegateId;
		this.keypairId = keypairId;
		this.notBefore = notBefore;
		this.notAfter = notAfter;
	}

	public Delegation(String delegateId, String keypairId, Long notBefore, Long notAfter) {
		this.delegateId = delegateId;
		this.keypairId = keypairId;
		this.notBefore = notBefore == null ? null : new Date(notBefore);
		this.notAfter = notAfter == null ? null : new Date(notAfter);
	}

	public Delegation(String delegateId, String keypairId, Date notAfter) {
		this(delegateId, keypairId, (Date) null, notAfter);
	}

	public Delegation(String delegateId, String keypairId, Long notAfter) {
		this.delegateId = delegateId;
		this.keypairId = keypairId;
		this.notBefore = null;
		this.notAfter = notAfter == null ? null : new Date(notAfter);
	}

	public Delegation(String delegateId, String keypairId) {
		this(delegateId, keypairId, (Date) null, (Date) null);
	}

	public String getDelegateId() {
		return delegateId;
	}

	public String getKeypairId() {
		return keypairId;
	}

	public Date getNotBefore() {
		return notBefore;
	}

	public Date getNotAfter() {
		return notAfter;
	}

	public boolean isValid() {
		Date now = new Date();
		return now.after(this.notBefore) && now.before(this.notAfter);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append(" [");
		sb.append("\n\tKeypair ID: ").append(this.keypairId);
		sb.append("\n\tDomain ID: ").append(this.delegateId);
		if (this.notBefore != null)
			sb.append("\n\tNot Before: ").append(this.notBefore.toString()).append(" - ")
					.append(this.notBefore.getTime());
		if (this.notAfter != null)
			sb.append("\n\tNot After: ").append(this.notAfter.toString()).append(" - ").append(this.notAfter.getTime());
		sb.append("\n]");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONAware jsonEncode() {
		JSONObject root = new JSONObject();
		root.put("keypair", this.keypairId);
		root.put("delegate", this.delegateId);
		if (this.notBefore != null)
			root.put("notBefore", this.notBefore.getTime());
		if (this.notAfter != null)
			root.put("notAfter", this.notAfter.getTime());
		return root;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegateId == null) ? 0 : delegateId.hashCode());
		result = prime * result + ((keypairId == null) ? 0 : keypairId.hashCode());
		result = prime * result + ((notAfter == null) ? 0 : notAfter.hashCode());
		result = prime * result + ((notBefore == null) ? 0 : notBefore.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Delegation other = (Delegation) obj;
		if (delegateId == null) {
			if (other.delegateId != null)
				return false;
		} else if (!delegateId.equals(other.delegateId))
			return false;
		if (keypairId == null) {
			if (other.keypairId != null)
				return false;
		} else if (!keypairId.equals(other.keypairId))
			return false;
		if (notAfter == null) {
			if (other.notAfter != null)
				return false;
		} else if (!notAfter.equals(other.notAfter))
			return false;
		if (notBefore == null) {
			if (other.notBefore != null)
				return false;
		} else if (!notBefore.equals(other.notBefore))
			return false;
		return true;
	}

}
