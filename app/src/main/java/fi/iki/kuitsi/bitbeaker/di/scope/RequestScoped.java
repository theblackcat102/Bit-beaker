package fi.iki.kuitsi.bitbeaker.di.scope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestScoped {
	Class<?> value();
}
