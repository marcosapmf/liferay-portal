FROM liferay/dxp:2026.q1.5-lts

COPY --chown=liferay:liferay configs/local/portal-ext.properties /opt/liferay/portal-ext.properties
COPY --chown=liferay:liferay bundles/osgi/modules/ /opt/liferay/osgi/modules/
COPY --chown=liferay:liferay bundles/osgi/client-extensions/ /opt/liferay/osgi/client-extensions/