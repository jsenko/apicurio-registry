import React, { FunctionComponent, useState } from "react";
import { Avatar, Dropdown, DropdownItem, DropdownList, MenuToggle, MenuToggleElement } from "@patternfly/react-core";
import { AuthService, useAuth } from "@apicurio/common-ui-components";
import { UserService, useUserService } from "@services/useUserService.ts";
import { ConfigService, useConfigService } from "@services/useConfigService.ts";


export type AvatarDropdownProps = object;


export const AvatarDropdown: FunctionComponent<AvatarDropdownProps> = () => {
    const [isOpen, setIsOpen] = useState(false);

    const config: ConfigService = useConfigService();
    const auth: AuthService = useAuth();
    const user: UserService = useUserService();

    const avatarSrc: string = `${config.uiContextPath() || "/"}avatar.png`;

    const onSelect = (): void => {
        setIsOpen(!isOpen);
    };

    const onToggle = (): void => {
        setIsOpen(!isOpen);
    };

    const icon = (
        <Avatar src={avatarSrc} alt="User" />
    );

    return (
        <Dropdown
            isOpen={isOpen}
            onSelect={onSelect}
            onOpenChange={(isOpen: boolean) => setIsOpen(isOpen)}
            toggle={(toggleRef: React.Ref<MenuToggleElement>) => (
                <MenuToggle
                    ref={toggleRef}
                    onClick={onToggle}
                    isFullHeight={true}
                    isExpanded={isOpen}
                    icon={icon}
                >
                    {
                        user.currentUser().displayName || "User"
                    }
                </MenuToggle>
            )}
            shouldFocusToggleOnSelect
        >
            <DropdownList>
                <DropdownItem
                    key="link"
                    // Prevent the default onClick functionality for example purposes
                    onClick={(ev: any) => {
                        auth.logout();
                        ev.preventDefault();
                    }}
                >
                    Logout
                </DropdownItem>
            </DropdownList>
        </Dropdown>
    );

};
