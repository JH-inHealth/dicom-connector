package org.mule.module.dicom.internal.config;

import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;

import javax.inject.Inject;
import java.util.*;

public class GetScuResultsValueProvider implements ValueProvider {
    @Inject
    private ConfigurationComponentLocator configurationComponentLocator;

    @Override
    public Set<Value> resolve() {
        List<String> componentList = new ArrayList<>();
        if (configurationComponentLocator != null) {
            List<ComponentLocation> allLocations = configurationComponentLocator.findAllLocations();
            for (ComponentLocation location : allLocations) {
                String namespace = location.getComponentIdentifier().getIdentifier().getNamespace();
                String name = location.getComponentIdentifier().getIdentifier().getName();
                if (namespace.equals("dicom") && name.equals("get-scu-results")) {
                    String locationName = location.getLocation();
                    componentList.add(locationName.substring(0, locationName.indexOf('/')));
                }
            }
        }
        return ValueBuilder.getValuesFor(componentList);
    }
}
