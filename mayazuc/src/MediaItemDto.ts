export class MediaItemDto {
    mediaId: string = "";
    title: string = "";
    imageUrl: string = "";
    isSelected: boolean = false;
    type: string = "folder";
}

export const FODLER_TYPE_MIXED = "FOLDER_TYPE_MIXED";
export const FOLDER_TYPE_TITLES = "FOLDER_TYPE_TITLES";
export const FOLDER_TYPE_NONE = "FOLDER_TYPE_NONE";