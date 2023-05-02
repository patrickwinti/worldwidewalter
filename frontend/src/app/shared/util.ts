export function isNonEmptyString(input: any): boolean {
  return input !== undefined && typeof(input) === 'string' && input.trim().length > 0;
}

export function containsNonEmptyString(listOfStrings: string[]): boolean {
  for (let s of listOfStrings) {
    if (isNonEmptyString(s)) {
      return true;
    }
  }
  return false;
}
