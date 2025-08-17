import dynamic from 'next/dynamic';
import React from 'react';

const CSR = (props: { children: React.ReactNode }) => {
  return <>{props.children}</>;
};

export default dynamic(() => Promise.resolve(CSR), {
  ssr: false,
});
