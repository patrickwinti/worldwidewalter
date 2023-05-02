import { containsNonEmptyString, isNonEmptyString } from "./util";

describe('isNonEmptyString', () => {
  it('should return false for empty string', () => {
    expect(isNonEmptyString('')).toBeFalse();
  });

  it('should return false for undefined', () => {
    expect(isNonEmptyString(undefined)).toBeFalse();
  });

  it('should return false for number', () => {
    expect(isNonEmptyString(3)).toBeFalse();
  });

  it('should return true for "a"', () => {
    expect(isNonEmptyString('a')).toBeTrue();
  });
});

describe('containsNonEmptyString', () => {
  it('should return false for empty array', () => {
    expect(containsNonEmptyString([])).toBeFalse();
  });

  it('should return false for array with empty strings', () => {
    expect(containsNonEmptyString(['', ''])).toBeFalse();
  });

  it('should return true if one entry contains non empty string', () => {
    expect(containsNonEmptyString(['', 'a'])).toBeTrue();
  });
});
