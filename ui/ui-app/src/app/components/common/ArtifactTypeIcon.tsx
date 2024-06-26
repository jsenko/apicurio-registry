import { FunctionComponent } from "react";
import "./ArtifactTypeIcon.css";
import { ArtifactTypes } from "@services/useArtifactTypesService.ts";

/**
 * Properties
 */
export type ArtifactTypeIconProps = {
    artifactType: string;
};

/**
 * Models the list of artifacts.
 */
export const ArtifactTypeIcon: FunctionComponent<ArtifactTypeIconProps> = (props: ArtifactTypeIconProps) => {

    return (
        <div className={ArtifactTypes.getClassNames(props.artifactType)} title={ArtifactTypes.getTitle(props.artifactType)} />
    );

};

