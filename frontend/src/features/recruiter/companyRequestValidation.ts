import type { RequestCreateCompanyRequest } from './recruiterTypes'

export type CompanyRequestFieldErrors = Partial<
    Record<keyof RequestCreateCompanyRequest, string>
>

export function isValidHttpUrl(value: string): boolean {
    try {
        const url = new URL(value.trim())
        return (url.protocol === 'http:' || url.protocol === 'https:') && Boolean(url.hostname)
    } catch {
        return false
    }
}

export function validateCompanyRequest(
    request: RequestCreateCompanyRequest,
): CompanyRequestFieldErrors {
    const errors: CompanyRequestFieldErrors = {}
    const name = request.name?.trim() ?? ''
    const taxCode = request.taxCode?.trim() ?? ''
    const website = request.website?.trim() ?? ''
    const companySize = request.companySize?.trim() ?? ''
    const address = request.address?.trim() ?? ''
    const city = request.city?.trim() ?? ''
    const logoUrl = request.logoUrl?.trim() ?? ''

    if (!name) {
        errors.name = 'Tên công ty không được để trống.'
    } else if (name.length > 200) {
        errors.name = 'Tên công ty không được vượt quá 200 ký tự.'
    }

    if (taxCode.length > 50) {
        errors.taxCode = 'Mã số thuế không được vượt quá 50 ký tự.'
    }

    if (website.length > 255) {
        errors.website = 'Website không được vượt quá 255 ký tự.'
    } else if (website && !isValidHttpUrl(website)) {
        errors.website = 'Nhập địa chỉ website đầy đủ, bắt đầu bằng https:// hoặc http://.'
    }

    if (companySize.length > 50) {
        errors.companySize = 'Quy mô công ty không được vượt quá 50 ký tự.'
    }

    if (address.length > 255) {
        errors.address = 'Địa chỉ không được vượt quá 255 ký tự.'
    }

    if (city.length > 100) {
        errors.city = 'Thành phố không được vượt quá 100 ký tự.'
    }

    if (logoUrl.length > 500) {
        errors.logoUrl = 'URL logo không được vượt quá 500 ký tự.'
    } else if (logoUrl && !isValidHttpUrl(logoUrl)) {
        errors.logoUrl = 'Logo cần là đường dẫn ảnh công khai bắt đầu bằng https:// hoặc http://.'
    }

    return errors
}
