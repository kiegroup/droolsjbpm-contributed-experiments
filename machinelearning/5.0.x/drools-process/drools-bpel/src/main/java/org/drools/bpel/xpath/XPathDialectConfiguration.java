package org.drools.bpel.xpath;

import org.drools.compiler.Dialect;
import org.drools.compiler.DialectConfiguration;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.compiler.PackageRegistry;
import org.drools.rule.Package;

public class XPathDialectConfiguration implements DialectConfiguration {

	private PackageBuilderConfiguration packageBuilderConfiguration;
	
	public PackageBuilderConfiguration getPackageBuilderConfiguration() {
		return packageBuilderConfiguration;
	}

	public void init(PackageBuilderConfiguration configuration) {
		this.packageBuilderConfiguration = configuration;
	}

	public Dialect newDialect(PackageBuilder packageBuilder, PackageRegistry pkgRegistry, Package pkg) {
		return new XPathDialect();
	}

}
