const { withNx } = require(' @nx/next/with-nx');

/**
 * @Type {import(' @nx/next/plugins/with-nx').WithNxOptions}
 **/
const nextConfig = {
  nx: {
    // Set this to true if you would like to use SVGR
    // See: https://github.com/gregberge/svgr
    svgr: false,
  },
  // Tu configuración de Next.js va aquí si tienes alguna.
  // Por ejemplo:
  // reactStrictMode: true,
};

module.exports = withNx(nextConfig);