package es.hefame.keyvault.util.exception;

import es.hefame.hcore.http.HttpException;

public class NoSuchAcronymException extends HttpException {
	private static final long serialVersionUID = 9023256654650508947L;

	public NoSuchAcronymException(String acronymType, String value) {
		super(400,
				'\'' + acronymType + '\'' + (value == null ? " no especificado" : " [ " + value + " ] no soportado"));
	}

	public NoSuchAcronymException(String acronymType) {
		this(acronymType, null);
	}

	public NoSuchAcronymException(Class<?> acronymType, String value) {
		this(acronymType.getSimpleName(), value);
	}

	public NoSuchAcronymException(Class<?> acronymType) {
		this(acronymType.getSimpleName());
	}

	public NoSuchAcronymException(Object acronymType, String value) {
		this(acronymType.getClass(), value);
	}

	public NoSuchAcronymException(Object acronymType) {
		this(acronymType.getClass());
	}

}
