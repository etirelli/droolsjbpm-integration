package org.kie.server.services.impl;

import java.util.List;

public interface KieContainersRegistry {

    /**
     * Adds a KieContainerInstance to the registry if one does not exists yet.
     * @param containerId the id of the container
     * @param ci the container instance
     * @return null if it was added or the previous instance if one exists
     */
    KieContainerInstance addIfDoesntExist(String containerId, KieContainerInstance ci);

    /**
     * Returns the container instance for the given container id
     * @param id
     * @return
     */
    KieContainerInstance getContainer(String containerId);

    /**
     * Removes container instance from the registry
     * @param containerId
     * @return the instance or null if it did not exist
     */
    KieContainerInstance removeContainer(String containerId);

    /**
     * Returns a list of all instantiated containers
     * @return
     */
    List<KieContainerInstance> getContainers();


}
